import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AssignmentRoll09 {

    public static void main(String[] args) throws IOException {
        File file = new File("input.txt");

        BufferedReader br = new BufferedReader(new FileReader(file));

        int quanta = Integer.parseInt(br.readLine().replaceAll("\\s", "").split(",")[1]);
        //System.out.println(quanta);

        ArrayList<Process> processes = new ArrayList<Process>();
        ArrayList<ProcessFCFS> processesfcfs = new ArrayList<ProcessFCFS>();

        String st;
        while ((st = br.readLine()) != null) {
            st = st.replaceAll("\\s", "");

            String[] out = st.split(",");
            int mem[] = new int[out.length - 3];
            for (int i = 3; i < out.length; i++) {
                mem[i - 3] = Integer.parseInt(out[i]);
            }
            processes.add(new Process(Integer.parseInt(out[0]), Integer.parseInt(out[1]), Integer.parseInt(out[2]), mem));
            processesfcfs.add(new ProcessFCFS(Integer.parseInt(out[0]), Integer.parseInt(out[1]), Integer.parseInt(out[2]), mem));
        }


        FCFS f = new FCFS(processesfcfs);
        f.getCalculations();
        double ref = f.getAvgTurnAround();
        printGain(ref, ref);

        RoundRobin rr = new RoundRobin(processes, quanta);
        rr.getTimeCalculations();
        double rta = rr.getAvgTurnAround();
        printGain(ref, rta);

        SJF sjf = new SJF(processes);
        sjf.getTimeCalculations();
        double sta = sjf.getAvgTurnAround();
        printGain(ref, sta);
    }

    private static void printGain(double refTA, double TA) {
        double gain=((refTA - TA)/(double) TA)*100.00;
        System.out.println(String.format("%.2f",gain));
    }

}


class SJF {

    int n, totalPF = 0;
    int[] pfs;
    private int[] pid, arrivalTime, burstTime, completeTime, turnAroundTime, waitingTime, flag;
    int sysTime = 0, tot = 0;
    float avgWaiting = 0, avgTurnAround = 0;


    public SJF(ArrayList<Process> processes) {

        this.n = processes.size();
        this.pid = new int[n];
        this.arrivalTime = new int[n];
        this.burstTime = new int[n];
        this.completeTime = new int[n];
        this.turnAroundTime = new int[n];
        this.waitingTime = new int[n];
        this.flag = new int[n];
        this.pfs = new int[n];

        for (int i = 0; i < n; i++) {
            Process proc = processes.get(i);
            arrivalTime[i] = proc.getArrivalTime();
            int numFrame = proc.getNumFrame();

            int[] pages = proc.getPages();

            int pageFaults = new LRU((int) Math.ceil(numFrame / 3.0)).getNoOfPageFaults(pages);
            pfs[i] = pageFaults;
            totalPF += pageFaults;
            burstTime[i] = 30 * pages.length + 60 * pageFaults;
            pid[i] = proc.getId();
            flag[i] = 0;
        }
    }


    public void getTimeCalculations() {

        boolean a = true;
        while (true) {
            int c = n, min = Integer.MAX_VALUE;
            if (tot == n)
                break;

            for (int i = 0; i < n; i++) {
               /* If i process at <= sys time flag=0 and burst<min
                process executed first */
                if ((arrivalTime[i] <= sysTime) && (flag[i] == 0) && (burstTime[i] < min)) {
                    min = burstTime[i];
                    c = i;
                }
            }

            if (c == n)
                sysTime++;
            else {
                completeTime[c] = sysTime + burstTime[c];
                sysTime += burstTime[c];
                turnAroundTime[c] = completeTime[c] - arrivalTime[c];
                waitingTime[c] = turnAroundTime[c] - burstTime[c];
                flag[c] = 1;
                tot++;
            }
        }
        for (int i = 0; i < n; i++) {
            avgWaiting += waitingTime[i];
            avgTurnAround += turnAroundTime[i];
//            System.out.println(pid[i] + "\t" + arrivalTime[i] + "\t" + burstTime[i] + "\t" + completeTime[i] + "\t" + turnAroundTime[i] + "\t" + waitingTime[i]);
        }

        int max = -1;
        for (int c = 1; c < completeTime.length; c++) {
            if (completeTime[c] > max) {
                max = completeTime[c];
            }
        }

        //getPrintedOutput(max);
        getFormattedOutput();


    }

    private void getPrintedOutput(int max) {
        System.out.println("SJF");
        System.out.println("Average tat : " + (float) (avgTurnAround / n));
        System.out.println("average waiting : " + (float) (avgWaiting / n));
        System.out.println("Completion : " + max);
        System.out.println("Total page fault : " + totalPF);
        System.out.println("Process page faults : ");
        for (int i : pfs) {
            System.out.print(i + " ");
        }
    }

    private void getFormattedOutput() {
        System.out.print("SJF " + String.format("%.2f", (double) (avgWaiting / n)) + ", " + String.format("%.2f",(double)(avgTurnAround / n)));
        System.out.print(", "+ totalPF+ ", ");
        for (int i=0; i<pfs.length;i++) {
            System.out.print(pfs[i] + ", ");
        }
    }

    public double getAvgTurnAround() {
        return (double)(avgTurnAround / n);
    }
}

class FCFS {

    int numProc, totalPF;
    int pfs[];
    ProcessFCFS[] processList;
    double totalWaiting = 0;
    double totalTurnAround = 0;

    FCFS(ArrayList<ProcessFCFS> processes) {
        numProc = processes.size();
        pfs = new int[numProc];
        this.processList = processes.toArray(new ProcessFCFS[numProc]);
        for (int i = 0; i < numProc; i++) {
            int pageFaults = new LRU((int) Math.ceil(processes.get(i).numPage / 3.0)).getNoOfPageFaults(processes.get(i).pages);
            pfs[i] = pageFaults;
            totalPF += pageFaults;
        }
    }

    void getCalculations() {

        PriorityQueue<ProcessFCFS> queue = new PriorityQueue<ProcessFCFS>(new procCompArrival());
        for (int i = 0; i < processList.length; i++) {
            queue.add(processList[i]);
        }

        int blockingTime = queue.peek().arrivalTime;
        int currentTime = 0;
        ProcessFCFS proc;

        while (!queue.isEmpty()) {
            proc = queue.remove();
            currentTime = Math.max(currentTime, proc.arrivalTime);

            if (proc.memRefs.isEmpty())
                continue;

            if (proc.isPageFault()) {
                proc.arrivalTime = blockingTime;
                blockingTime = Math.max(blockingTime, currentTime);
                blockingTime += 60;
                proc.handlePagefault();
                queue.add(proc);
            } else {
                currentTime += 30;
                proc.handlePagehit();

                while (!proc.memRefs.isEmpty() && !proc.isPageFault()) {
                    currentTime += 30;
                    proc.handlePagehit();
                }

                if (proc.memRefs.isEmpty()) {
                    totalWaiting += (currentTime - proc.firstArrival - proc.numMemRef * 30);
                    totalTurnAround += (currentTime - proc.firstArrival);
                }

                if (!proc.memRefs.isEmpty() && proc.isPageFault()) {
                    blockingTime = Math.max(blockingTime, currentTime);
                    blockingTime += 60;
                    proc.arrivalTime = blockingTime;
                    queue.add(proc);
                    proc.handlePagefault();
                }
            }
        }
        //getPrintedOutput(currentTime);
        getFormattedOutput();
    }

    public double getAvgTurnAround() {
        return totalTurnAround/numProc;
    }

    private void getFormattedOutput() {
        System.out.print("FCFS " + String.format("%.2f", totalWaiting / numProc) + ", " + String.format("%.2f", totalTurnAround / numProc));
        System.out.print(", "+ totalPF+ ", ");
        for (int i=0; i<pfs.length;i++) {
            System.out.print(pfs[i] + ", ");
        }
    }

    private void getPrintedOutput(int currentTime) {
        System.out.println("\n\nFCFS");
        System.out.println("Avg Turn Around Time: " + totalTurnAround / numProc);
        System.out.println("Avg Waiting Time: " + totalWaiting / numProc);
        System.out.println("Completion Time: " + currentTime);
        System.out.println("Total page fault : " + totalPF);
        System.out.println("Process page faults : ");
        for (int i : pfs) {
            System.out.print(i + " ");
        }


    }
}

class procCompArrival implements Comparator<ProcessFCFS> {
    @Override
    public int compare(ProcessFCFS o1, ProcessFCFS o2) {
        if (o1.arrivalTime >= o2.arrivalTime)
            return 1;
        else
            return o1.arrivalTime - o2.arrivalTime;
    }
}

class RoundRobin {
    int turnAroundTime = 0, runningTime = 0, waitingTime = 0, completionTime = 0;
    ArrayList<Process> processes;
    int numProcess, totalPF, quanta;
    int[] pfs;

    public RoundRobin(ArrayList<Process> processes, int quanta) {

        this.numProcess = processes.size();
        this.pfs = new int[numProcess];
        this.quanta = quanta;
        this.processes = processes;

        for (int i = 0; i < numProcess; i++) {
            processes.get(i).init();
            Process proc = processes.get(i);
            int numFrame = proc.getNumFrame();

            int[] pages = proc.getPages();

            int pageFaults = new LRU((int) Math.ceil(numFrame / 3.0)).getNoOfPageFaults(pages);
            pfs[i] = pageFaults;
            totalPF += pageFaults;
        }
    }

    void getTimeCalculations() throws IOException {

        PriorityQueue<Process> queue = new PriorityQueue<>(new processRRComparator());
        doInitCalculation();
        int blocked = 0;
        completionTime = 0;
        for (int i = 0; i < processes.size(); i++) {
            queue.add(processes.get(i));
        }
        while (!queue.isEmpty()) {
            Process proc = queue.peek();
            queue.remove();
            if (proc.currentTime > completionTime) {
                completionTime = proc.currentTime;
            }
            proc.roundRobinBlockFlag = 0;
            int est_time = proc.singleRoundRobinExecute(quanta);
            completionTime += est_time;

            if (proc.roundRobinBlockFlag == 1) {
                blocked = Math.max(blocked + 60, completionTime + 60);
                proc.currentTime = blocked;
                queue.add(proc);
            } else if (proc.isFinished() == 0) {
                proc.currentTime = completionTime;
                queue.add(proc);
            } else {
                runningTime += proc.getExecutionTime() + proc.getPageFaultTime();
                waitingTime += completionTime - proc.arrivalTime - proc.getExecutionTime();
                turnAroundTime += completionTime - proc.arrivalTime;
                blocked = Math.max(blocked, completionTime);

            }
        }
        //getPrintedOutput();
        getFormattedOutput();

//        double gain=((double)(turnAroundTime-turnAroundTime)/(double) turnAroundTime)*100.00;
//        System.out.println("performance gain of Round-Robin: "+ gain);
    }

    private void doInitCalculation() {
        for (int i = 0; i < processes.size(); i++) {
            processes.get(i).calculateTimeSlices(quanta);
            processes.get(i).pageFault = 0;
        }
    }

    private void getPrintedOutput() {
        System.out.println("\nRound-Robin");
        System.out.println("Avg Turn Time: " + turnAroundTime / (double) numProcess);
        System.out.println("Completion Time: " + completionTime);
        System.out.println("Avg Waiting Time: " + (double) waitingTime / numProcess);
        System.out.print("Total Page Fault : " + totalPF + "\nPage Fault Per Process: ");
        for (int i = 0; i < numProcess; i++) {
            System.out.print(pfs[i] + " ");
        }
        System.out.println("");
    }

    private void getFormattedOutput() {
        System.out.print("RRS " + String.format("%.2f", (double) waitingTime / numProcess) + ", " + String.format("%.2f", turnAroundTime / (double) numProcess));
        System.out.print(", "+ totalPF+ ", ");
        for (int i=0; i<pfs.length;i++) {
            System.out.print(pfs[i] + ", ");
        }
    }

    public double getAvgTurnAround() {
        return turnAroundTime / (double) numProcess;
    }
}

class processRRComparator implements Comparator<Process> {
    @Override
    public int compare(Process o1, Process o2) {
        if (o1.currentTime >= o2.currentTime)
            return 1;
        else
            return o1.currentTime - o2.currentTime;
    }
}


class LRU {
    int capacity;
    ArrayList<Integer> frames;
    int pageFaults = 0, count = 0;

    public LRU(int capacity) {
        this.capacity = capacity;
        this.frames = new ArrayList<>(capacity);
    }

    public int getNoOfPageFaults(int[] requests) {
        for (int i : requests) {
            addFrame(i);
        }
        return pageFaults;
    }

    void addFrame(int i) {
        if (!frames.contains(i)) {
            if (frames.size() == capacity) {
                frames.remove(0);
                frames.add(capacity - 1, i);
            } else
                frames.add(count, i);
            // Increment page faults
            pageFaults++;
            ++count;
        } else {
            frames.remove((Object) i);
            frames.add(frames.size(), i);
        }
    }

    void printFageFaults() {

    }
}


class Process {

    int id, numFrame, nextPage = 0, pageFault = 0, allocatedFrame;
    int arrivalTime, currentTime, roundRobinBlockFlag;
    int[] pageRefs, vrefs, remarr, frameRef, memRef, pages;
    private int pageSize = 512;

    public Process(int id, int numFrame, int arrivalTime, int[] memRefs) {
        this.id = id;
        this.numFrame = numFrame;

        this.arrivalTime = arrivalTime;
        this.currentTime = arrivalTime;
        this.pages = memToPage(memRefs);
        this.remarr = new int[memRefs.length];
        this.memRef = new int[memRefs.length];
        this.vrefs = new int[memRefs.length];
        this.frameRef = new int[memRefs.length];
        this.pageRefs = new int[memRefs.length];
        allocatedFrame = (int) Math.ceil((double) numFrame / 3.0);

        for (int i = 0; i < memRefs.length; i++) {
            memRef[i] = memRefs[i];
            pageRefs[i] = memRefs[i] / 512;
        }

        for (int i = 0; i < allocatedFrame; i++) {
            frameRef[i] = -1;
        }
    }

    public int getNumFrame() {
        return numFrame;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    private int[] memToPage(int[] memRef) {
        int[] pages = new int[memRef.length];
        for (int i = 0; i < memRef.length; i++) {
            pages[i] = memRef[i] / pageSize;
        }
        return pages;
    }

    void init() {
        for (int i = 0; i < allocatedFrame; i++) {
            frameRef[i] = -1;
        }
        currentTime = arrivalTime;
        nextPage = 0;
    }

    int getExecutionTime() {
        return memRef.length * 30;
    }

    int getPageFaultTime() {
        return pageFault * 60;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int[] getPages() {
        return pages;
    }

    public int getId() {
        return id;
    }

    int isFinished() {
        if (nextPage < pages.length) {
            return 0;
        }
        return 1;
    }

    public void calculateTimeSlices(int quan) {
        int p = (int) Math.ceil((double) 30 / quan);
        for (int i = 0; i < memRef.length; i++) {
            vrefs[i] = p;
            remarr[i] = 30;
        }
    }

    int getFreeFrame() {
        for (int i = 0; i < allocatedFrame; i++) {
            if (frameRef[i] == -1) {
                return i;
            }
        }
        return allocatedFrame - 1;
    }

    int isPageHit(int pg) {
        for (int i = 0; i < allocatedFrame; i++) {
            if (frameRef[i] == pg) {
                return i;
            }
        }
        return -1;
    }

    void updatePageTable(int ed) {
        for (int i = ed; i > 0; i--) {
            swap_frames(i, i - 1);
        }
    }

    private void swap_frames(int i, int j) {
        int tmp = frameRef[i];
        frameRef[i] = frameRef[j];
        frameRef[j] = tmp;
    }

    int singleRoundRobinExecute(int quanta) {
        int cost = 0;
        int remaining = quanta;
        for (; nextPage < pageRefs.length; ) {
            if (remaining == 0) {
                return cost;
            }
            int pr = pageRefs[nextPage];
            int fd = isPageHit(pr);

            if (fd != -1) {
                int tcost = Math.min(remarr[nextPage], remaining);
                cost += tcost;
                remaining -= tcost;
                remarr[nextPage] -= tcost;
                if (remarr[nextPage] == 0) updatePageTable(fd);
                if (remarr[nextPage] == 0) nextPage++;
            } else {
                int id = getFreeFrame();
                frameRef[id] = pr;
                updatePageTable(id);
                roundRobinBlockFlag = 1;
                return cost;
            }
        }
        return cost;
    }


}

class ProcessFCFS {

    int id, pageSize = 512;
    int numPage;
    int numMemRef;
    int arrivalTime;
    Queue<Integer> memRefs = new LinkedList<>();
    int pageFaults = 0;
    int firstArrival;
    int[] pages;
    ArrayList<Integer> pageTable = new ArrayList<Integer>();

    public ProcessFCFS(int id, int numPage, int arrivalTime, int[] mem) {
        this.id = id;
        this.numPage = numPage;
        this.numMemRef = mem.length;
        this.arrivalTime = arrivalTime;
        this.memRefs = new LinkedList<>();
        pages = new int[mem.length];
        for (int i = 0; i < mem.length; i++) {
            memRefs.add(mem[i] / pageSize);
            pages[i] = mem[i] / pageSize;
        }
        this.firstArrival = arrivalTime;
    }

    void handlePagehit() {
        int memoryReference = memRefs.peek();
        memRefs.remove();

        for (int j = 0; j < pageTable.size(); j++) {
            if (pageTable.get(j) == memoryReference) {
                pageTable.remove(j);
                break;
            }
        }
        pageTable.add(memoryReference);
    }

    boolean isPageFault() {
        return !pageTable.contains(memRefs.peek());
    }

    void handlePagefault() {
        int memoryReference = memRefs.peek();
        int frameSize = ((numPage - 1) / 3) + 1;
        if (pageTable.size() == frameSize) {
            pageTable.remove(0);
        }
        this.pageFaults++;
        pageTable.add(memoryReference);
    }

}

