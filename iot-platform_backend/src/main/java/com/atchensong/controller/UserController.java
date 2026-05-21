package com.atchensong.controller;

import com.atchensong.common.R;
import com.atchensong.pojo.User;
import com.atchensong.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R<User> login(@RequestBody User user) {
        log.info("[用户登录]:传入的用户信息{}", user);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User one = userService.getOne(queryWrapper);
        if (one == null) {
            return R.error("用户名不存在");
        }
        if (!one.getPassword().equals(user.getPassword())) {
            return R.error("密码错误");
    }
        log.info("[用户登录]:登录成功{}", user);
        return R.success(one);
    }
    // 1. 分页查询用户列表
    @GetMapping("/page")
    public R<Map<String, Object>> page(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String username,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String role) {

        Page<User> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(username), User::getUsername, username)
                .like(StringUtils.isNotBlank(name), User::getName, name)
                .eq(StringUtils.isNotBlank(role), User::getRole, role)
                .orderByDesc(User::getCreateTime);

        Page<User> result = userService.page(pageInfo, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());  // 改为前端期望的字段名
        data.put("total", result.getTotal());

        return R.success(data);
    }


    // 2. 新增用户
    @PostMapping
    public R<String> save(@RequestBody User user) {
        log.info("[新增用户] 用户信息: {}", user);

        // 检查用户名是否已存在
        if (userService.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .count() > 0) {
            return R.error("用户名已存在");
        }

        // 密码加密（实际项目应使用BCrypt等加密）
//        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        userService.save(user);
        return R.success("用户添加成功");
    }

    // 3. 更新用户信息
    @PutMapping
    public R<String> update(@RequestBody User user) {
        log.info("[更新用户] 用户信息: {}", user);

        // 密码为空时不更新密码
        if (StringUtils.isBlank(user.getPassword())) {
            user.setPassword(null);
        } else {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }

        userService.updateById(user);
        return R.success("用户信息更新成功");
    }

    // 4. 删除用户
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        log.info("[删除用户] ID: {}", id);

        // 防止删除admin用户
        User user = userService.getById(id);
        if (user != null && "admin".equals(user.getRole())) {
            return R.error("不允许删除管理员账户");
        }

        userService.removeById(id);
        return R.success("用户删除成功");
    }

    // 5. 修改用户状态
    @PutMapping("/status")
    public R<String> updateStatus(@RequestParam Long id,
                                  @RequestParam String status) {
        log.info("[修改状态] ID: {}, 新状态: {}", id, status);

        User user = new User();
        user.setId(Math.toIntExact(id));
        user.setStatus(status);
        userService.updateById(user);
        return R.success("用户状态更新成功");
    }

    // 6. 根据ID获取用户详情（用于编辑回显）
    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        log.info("[用户详情] ID: {}", id);
        User user = userService.getById(id);
        return user != null ? R.success(user) : R.error("用户不存在");
    }
}

