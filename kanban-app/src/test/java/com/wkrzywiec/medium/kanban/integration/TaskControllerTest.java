package com.wkrzywiec.medium.kanban.integration;

import com.wkrzywiec.medium.kanban.model.Task;
import com.wkrzywiec.medium.kanban.model.TaskDTO;
import com.wkrzywiec.medium.kanban.repository.TaskRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class TaskControllerTest {

    private String baseURL;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @Before
    public void setUp(){
        baseURL = "http://localhost:" + port;
    }

    @Test
    public void whenGetAllTasks_thenReceiveSingleTask(){

        //given
        Task task = saveSingleTask();

        //when
        ResponseEntity<List<Task>> response = this.restTemplate.exchange(
                baseURL + "tasks/",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                new ParameterizedTypeReference<List<Task>>() {});

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(task, response.getBody().get(0));
    }

    @Test
    public void whenGetSingleTaskById_thenReceiveSingleTask(){

        //given
        Task task = saveSingleTask();

        //when
        ResponseEntity<Task> response = this.restTemplate.exchange(
                baseURL + "tasks/" + task.getId(),
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                Task.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(task, response.getBody());
    }

    @Test
    public void whenPostSingleTask_thenItIsStoredInDb(){

        //given
        Task task = saveSingleTask();

        //when
        ResponseEntity<Task> response = this.restTemplate.exchange(
                baseURL + "tasks/",
                HttpMethod.POST,
                new HttpEntity<>(convertTaskToDTO(task), new HttpHeaders()),
                Task.class);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(task.toString(), response.getBody().toString());
        assertEquals(task.toString(), taskRepository.findById(response.getBody().getId()).get().toString());
    }

    @Test
    public void whenPutSingleTask_thenItIsUpdated(){

        //given
        Task task = saveSingleTask();
        task.setTitle(task.getTitle() + " Updated");

        //when
        ResponseEntity<Task> response = this.restTemplate.exchange(
                baseURL + "tasks/" + task.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(convertTaskToDTO(task), new HttpHeaders()),
                Task.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(task.getTitle(), taskRepository.findById(response.getBody().getId()).get().getTitle());
    }

    private Task createSingleTask(){
        Task task = new Task();
        int random = (int)(Math.random() * 100 + 1);
        task.setTitle("Test Task " + random);
        task.setDescription("Description " + random);
        task.setColor("Color " + random);
        return task;
    }

    private TaskDTO convertTaskToDTO(Task task) {
        return new TaskDTO().builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .color(task.getColor())
                .build();
    }

    private Task saveSingleTask(){
        return taskRepository.save(createSingleTask());
    }
}
