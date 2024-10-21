/*
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.streamreasoning.polyflow.base.processing.TaskImpl;
import org.streamreasoning.polyflow.base.stream.defaultDataStream;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UnregisterTaskTest {
    Task<String, String, List<String>, String> task1 = new TaskImpl<>("1");
    Task<String, String, List<String>, String> task2 = new TaskImpl<>("2");
    ContinuousProgram<String, String, List<String>, String> cp = new ContinuousProgramImpl<>();
    DataStream<String> input1 = new defaultDataStream<>("input1");
    DataStream<String> output1 = new defaultDataStream<>("output1");

     @BeforeEach
     void setup(){
        task1 = new TaskImpl<>("1");
        task2 = new TaskImpl<>("2");
        cp = new ContinuousProgramImpl<>();
        input1 = new defaultDataStream<>("input1");
        output1 = new defaultDataStream<>("output1");
        cp.buildTask(task1, Collections.singletonList(input1), Collections.singletonList(output1));
        cp.buildTask(task2, Collections.singletonList(input1), Collections.singletonList(output1));
    }

    @Test
    public void duplicateId(){
         Task<String, String, List<String>, String> dummyTask = new TaskImpl<>("1");
         RuntimeException exception = assertThrows(
                RuntimeException.class,
                ()->{cp.buildTask(dummyTask,
                        Collections.singletonList(input1),
                        Collections.singletonList(output1));});
         assertEquals("Task with ID "+dummyTask.getId()+" already exists!", exception.getMessage());
    }

}
*/
