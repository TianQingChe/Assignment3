import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jfree.ui.RefineryUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class Application {

    private static List<Future<CustomResponse>> resultList = new ArrayList<Future<CustomResponse>>();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //default value
        int maxThreadNumber = 256;
//        String url = "http://34.226.31.240:8080/data";
        String fileName = "lambda"+maxThreadNumber+".adt";
        String url = "https://e-analogy-223702.appspot.com";
//        String url = "http://127.0.0.1:8080";
        int dayNumber = 100;
        int userPopulation = 100000;
        int testsPerPhase = 30;

//        maxThreadNumber = Integer.valueOf(args[0]);
//        url = args[1];
//        dayNumber = Integer.valueOf(args[2]);
//        userPopulation = Integer.valueOf(args[3]);
//        testsPerPhase = Integer.valueOf(args[4]);

        final ExecutorService threadPool = Executors.newFixedThreadPool(maxThreadNumber);

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(2000);
        connManager.setDefaultMaxPerRoute(2000);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Client starting...... Time:" + df.format(new Date()));
        long start = System.currentTimeMillis();

        int threadNumber;
        int iterNumber;
        int interval_start;
        int interval_end;
        //warmup
        System.out.println("Warmup phase start: " + df.format(new Date()));
        threadNumber = (int)(maxThreadNumber * 0.1);
        CountDownLatch latch = new CountDownLatch(threadNumber);
        iterNumber = testsPerPhase * 3;
        interval_start = 0;
        interval_end = 2;
        doTask(connManager,threadNumber,url,iterNumber,dayNumber,userPopulation,threadPool,interval_start,interval_end,latch);
        latch.await();

        //loading
        System.out.println("Loading phase start: " + df.format(new Date()));
        threadNumber = (int)(maxThreadNumber * 0.5);
        latch = new CountDownLatch(threadNumber);
        iterNumber = testsPerPhase * 5;
        interval_start = 3;
        interval_end = 7;
        doTask(connManager,threadNumber,url,iterNumber,dayNumber,userPopulation,threadPool,interval_start,interval_end,latch);
        latch.await();

        //peak
        System.out.println("Peak phase start: " + df.format(new Date()));
        threadNumber = maxThreadNumber;
        latch = new CountDownLatch(threadNumber);
        iterNumber = testsPerPhase * 11;
        interval_start = 8;
        interval_end = 18;
        doTask(connManager,threadNumber,url,iterNumber,dayNumber,userPopulation,threadPool,interval_start,interval_end,latch);
        latch.await();

        //cooldown
        System.out.println("Cooldown phase start: " + df.format(new Date()));
        threadNumber = (int)(maxThreadNumber * 0.25);
        latch = new CountDownLatch(threadNumber);
        iterNumber = testsPerPhase * 5;
        interval_start = 19;
        interval_end = 23;
        doTask(connManager,threadNumber,url,iterNumber,dayNumber,userPopulation,threadPool,interval_start,interval_end,latch);
        latch.await();

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(30000, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        //end
        System.out.println("================================================");
        String totalSeconds = String.format("%.3f", (System.currentTimeMillis() - start)*10 / 1000.0);

        //postprocess
        int requestCounter = 0;
        int successCounter = 0;
        List<Latency> serialList = new ArrayList<>();
        List<Long> latencies = new ArrayList<>();

        for(Future<CustomResponse> future : resultList){
            CustomResponse customResponse = future.get();
            requestCounter += customResponse.requestCount;
            successCounter += customResponse.successCount;
            for(Latency latency : customResponse.latencyLst){
                latencies.add(latency.latency);
                serialList.add(latency);
                System.out.println(latency.startTime + ": " + latency.latency);
            }
        }

        //serialize data
        File file = new File(fileName);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            //将List转换成数组
            Latency[] obj = new Latency[serialList.size()];
            serialList.toArray(obj);
            //执行序列化存储
            out.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(latencies);

        //output
        System.out.println("Test Wall Time: " + totalSeconds + " seconds");
        System.out.println("Total number of requests sent: " + requestCounter);
        System.out.println("Total number of Successful responses: " + successCounter);
        System.out.println("Overall throughput across all phases: " + requestCounter + " requests/" + totalSeconds + " seconds");
        System.out.println("Mean latency: " + getMeanLatency(latencies)*10 + "ms");
        System.out.println("Median latency: " + getMedianLatency(latencies)*10 + "ms");
        System.out.println("95th percentile latency: " + get95Percent(latencies)*10 + "ms");
        System.out.println("99th percentile latency: " + get99Percent(latencies)*10 + "ms");

        int[] buckets = Plot.getBuckets(fileName);
        Plot chart = new Plot(
                "Throughtput Chart" ,
                "Overall Throughput+"+ maxThreadNumber +"threads 100 iterations",buckets);
        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
    }

    private static void doTask(PoolingHttpClientConnectionManager connManager,int threadNum,String url,int iterNum, int dayNumber, int userPopulation,ExecutorService threadPool,int interval_start, int interval_end,CountDownLatch latch){

        for(int i = 0; i < threadNum; i++){
            System.out.println("doing");
            Future<CustomResponse> customResponseFuture = threadPool.submit(new MyClient(connManager,url,iterNum,dayNumber,userPopulation,interval_start,interval_end,latch));
            resultList.add(customResponseFuture);
        }
    }

    private static long get95Percent(List<Long> latencyLst) {
        int index = (int) (latencyLst.size() * 0.95);
        return latencyLst.get(index);
    }

    private static long get99Percent(List<Long> latencyLst) {
        int index = (int) (latencyLst.size() * 0.99);
        return latencyLst.get(index);
    }

    private static long getMeanLatency(List<Long> latencyLst) {
        long sum = 0;
        for (int i = 0; i < latencyLst.size(); i++) {
            sum += latencyLst.get(i);
        }
        return (sum / latencyLst.size());
    }

    private static long getMedianLatency(List<Long> latencyLst) {
        int size = latencyLst.size();
        int mid = size / 2;
        if (size % 2 == 1) {
            return latencyLst.get(mid);
        } else {
            int mid2 = mid - 1;
            return (latencyLst.get(mid) + latencyLst.get(mid2)) / 2;
        }
    }

}
