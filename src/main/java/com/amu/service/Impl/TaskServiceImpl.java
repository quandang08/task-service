package com.amu.service.Impl;

import com.amu.dto.UserDto;
import com.amu.entities.Task;
import com.amu.entities.TaskStatus;
import com.amu.exception.ForbiddenException;
import com.amu.repositories.TaskRepository;
import com.amu.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public Task createTask(Task task, String requesterRole) {
        if (requesterRole == null || !requesterRole.equals("ROLE_ADMIN")) {
            throw new ForbiddenException("Permission denied");
        }
        task.setAssignedUserId(null);
        task.setCreateAt(LocalDateTime.now());
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }

        return taskRepository.save(task);
    }

    @Override
    public Task getTaskById(Long id) throws Exception {
        return taskRepository.findById(id).orElseThrow(() -> new Exception("Task not found"));
    }

    @Override
    public List<Task> getAllTask(TaskStatus status) {
        return taskRepository.findAll().stream()
                .filter(task -> status == null || task.getStatus().name().equalsIgnoreCase(status.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public Task updateTask(Long id, Task updateTask, Long userId) throws Exception {

        Task existingTask = getTaskById(id);
        UserDto user = new UserDto();
        if (existingTask == null) {
            throw new Exception("Task not found");
        }

         //Kiểm tra quyền hạn của người dùng, chỉ cho phép chủ sở hữu task sửa đổi
        if (!user.getId().equals(userId)) {
            throw new Exception("You do not have permission to update this task");
        }

        if (updateTask.getTitle() != null) {
            existingTask.setTitle(updateTask.getTitle());
        }
        if (updateTask.getImage() != null) {
            existingTask.setImage(updateTask.getImage());
        }
        if (updateTask.getDescription() != null) {
            existingTask.setDescription(updateTask.getDescription());
        }
        if (updateTask.getStatus() != null) {
            existingTask.setStatus(updateTask.getStatus());
        }
        if (updateTask.getDeadline() != null) {
            existingTask.setDeadline(updateTask.getDeadline());
        }

        return taskRepository.save(existingTask);
    }


    @Override
    public void deleteTask(Long id, UserDto user) throws Exception {
        Task task = getTaskById(id);

        boolean isOwner = task.getAssignedUserId() != null && task.getAssignedUserId().equals(user.getId());
        boolean isAdmin = user.getRole().equalsIgnoreCase("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new Exception("Bạn không có quyền xóa task này.");
        }

        taskRepository.deleteById(id);
    }

    @Override
    public Task assignTask(Long taskId, Long userId) throws Exception {
        Task task = getTaskById(taskId);

        if (task.getAssignedUserId() != null) {
            throw new Exception("Task đã có người nhận");
        }
        task.setAssignedUserId(userId);
        task.setStatus(TaskStatus.ASSIGNED);

        return taskRepository.save(task);
    }


    @Override
    public List<Task> assignedUsersTask(Long userId, TaskStatus taskStatus) throws Exception {
        // Lấy tất cả các task được giao cho người dùng
        List<Task> allTasks = taskRepository.findByAssignedUserId(userId);

        // Nếu không có task nào được giao
        if (allTasks.isEmpty()) {
            return allTasks; // Trả về danh sách trống
        }

        // Nếu có trạng thái task được cung cấp, lọc các task theo trạng thái
        if (taskStatus != null) {
            allTasks = allTasks.stream()
                    .filter(task -> task.getStatus() == taskStatus)
                    .collect(Collectors.toList());
        }

        return allTasks;
    }

    @Override
    public void completeTask(Long taskId) throws Exception {
        Task task = getTaskById(taskId);
        // Kiểm tra trạng thái task
        if (task.getStatus() == TaskStatus.DONE) {
            throw new Exception("Task đã được hoàn thành.");
        }
        if (task.getStatus() == TaskStatus.PENDING) {
            throw new Exception("Task chưa được giao cho người dùng.");
        }

        // Cập nhật trạng thái task thành DONE
        task.setStatus(TaskStatus.DONE);
        taskRepository.save(task);
    }
}
