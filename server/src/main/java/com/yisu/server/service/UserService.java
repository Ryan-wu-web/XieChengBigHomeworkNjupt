package com.yisu.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yisu.server.common.LoginResult;
import com.yisu.server.entity.User;

public interface UserService extends IService<User> {
    LoginResult login(User user);
    void register(User user);
    User getCurrentUser(Integer userId);
    void updateProfile(User user);
    void updatePassword(Integer userId, String oldPassword, String newPassword);
    IPage<User> getMerchantList(Integer pageNum, Integer pageSize, String username);
    IPage<User> getConsumerList(Integer pageNum, Integer pageSize, String username);
    void deleteUser(Integer userId);
    void updateUserByAdmin(User user);
    void toggleUserStatus(Integer userId, Integer status);
}
