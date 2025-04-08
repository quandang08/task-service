package com.amu.controller;

import com.amu.dto.UserDto;
import com.amu.entities.Task;
import com.amu.entities.TaskStatus;
import com.amu.exception.UnauthorizedException;
import com.amu.service.TaskService;
import com.amu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Task> createTask (@RequestBody Task task,
                                            @RequestHeader("Authorization") String token) throws Exception {
        UserDto user = userService.getUserProfile(token);
        Task createdTask = taskService.createTask(task, user.getRole());

        return new ResponseEntity<>(createdTask,HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ) throws Exception {
        UserDto user = userService.getUserProfile(token);
        Task task = taskService.getTaskById(id);

        return ResponseEntity.ok(task);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Task>> getAssignedTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        UserDto user = userService.getUserProfile(token);

        // 	Lấy task đã giao cho user hiện tại (người dùng bình thường)
        List<Task> tasks = taskService.assignedUsersTask(user.getId(), status);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping()
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        UserDto user = userService.getUserProfile(token);

        //Lấy tất cả task (Admin / Manager có quyền thấy hết)
        List<Task> tasks = taskService.getAllTask(status);

        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}/user/{userid}/assigned")
    public ResponseEntity<Task> assignedTaskToUser(
            @PathVariable("id") Long id,
            @PathVariable("userid") Long userId,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        UserDto user = userService.getUserProfile(token);

        // Giao nhiệm vụ cho user và cập nhật task
        Task task = taskService.assignTask(id, userId);

        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task task,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        UserDto user = userService.getUserProfile(token);

        //Chỉ vai trò admin được quyền cập nhật
        if (!user.getRole().equals("ROLE_ADMIN") && !taskService.isTaskOwner(id, user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền sửa công việc này.");
        }

        Task updated = taskService.updateTask(id, task, user.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        UserDto user = userService.getUserProfile(token);
        taskService.deleteTask(id, user);
        return ResponseEntity.ok("Xóa task thành công.");
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completeTask(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        UserDto user = userService.getUserProfile(token);

        if (!taskService.isTaskAssignedToUser(id, user.getId())) {
            throw new UnauthorizedException("Bạn không thể hoàn thành công việc này.");
        }

        String message = taskService.completeTask(id);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
