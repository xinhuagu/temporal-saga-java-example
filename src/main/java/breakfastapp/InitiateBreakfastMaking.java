package breakfastapp;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;


import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class InitiateBreakfastMaking {

    public static void main(String[] args) throws Exception {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(Shared.BREAKFAST_TASK_QUEUE)
                .setWorkflowId("breakfast-workflow")
                .build();
        WorkflowClient client = WorkflowClient.newInstance(service);
        BreakfastWorkflow workflow = client.newWorkflowStub(BreakfastWorkflow.class, options);
        boolean parallel = args.length > 0 && (args[0].equals("--parallel-compensations") || args[0].equals("-p"));
        WorkflowExecution we = WorkflowClient.start(workflow::makeBreakfast, parallel);
        System.out.printf("\nWorkflowID: %s RunID: %s", we.getWorkflowId(), we.getRunId());


        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(Shared.BREAKFAST_TASK_QUEUE);
        // This Worker hosts both Workflow and Activity implementations.
        // Workflows are stateful so a type is needed to create instances.
        worker.registerWorkflowImplementationTypes(BreakfastWorkflowImpl.class);
        worker.registerActivitiesImplementations(new BreakfastActivityImpl());
        // Start listening to the Task Queue.
        factory.start();


        System.exit(0);
    }
}