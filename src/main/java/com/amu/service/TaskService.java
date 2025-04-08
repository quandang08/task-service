package com.amu.service;

import com.amu.dto.UserDto;
import com.amu.entities.Task;
import com.amu.entities.TaskStatus;

import java.util.List;

public interface TaskService {
    Task createTask(Task task, String requesterRole) throws Exception;

    Task getTaskById(Long id) throws Exception;

    List<Task> getAllTask(TaskStatus status);

    Task updateTask(Long id, Task updateTask, Long userId) throws Exception;

    void deleteTask(Long id, UserDto userDto) throws Exception;

    Task assignTask(Long taskId, Long userId) throws Exception;

    List<Task> assignedUsersTask(Long userId, TaskStatus taskStatus) throws Exception;

    String completeTask(Long taskId) throws Exception;

    boolean isTaskAssignedToUser(Long id, Long id1);

    boolean isTaskOwner(Long id, Long id1);

    //Page<TaskResponse> searchTasks(String keyword, TaskStatus status, Long assigneeId, int page, int size);
    //Page<TaskResponse> getAllTask(TaskStatus status, int page, int size);
}
