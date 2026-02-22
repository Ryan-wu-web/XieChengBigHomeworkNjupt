package com.yisu.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yisu.server.common.JwtUtils;
import com.yisu.server.common.LoginResult;
import com.yisu.server.common.Result;
import com.yisu.server.entity.Hotel;
import com.yisu.server.entity.RoomType;
import com.yisu.server.entity.User;
import com.yisu.server.service.HotelService;
import com.yisu.server.service.RoomTypeService;
import com.yisu.server.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private RoomTypeService roomTypeService;

    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody User user) {
        try {
            LoginResult result = userService.login(user);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Object> register(@RequestBody User user) {
        try {
            userService.register(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // Get user info from token
    @GetMapping("/info")
    public Result<User> info(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null) {
                return Result.error("Token无效");
            }
            Integer userId = (Integer) claims.get("userId");
            User user = userService.getCurrentUser(userId);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Update user profile (avatar, email, phone)
    @PutMapping("/profile")
    public Result<Object> updateProfile(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                        @RequestBody User user) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null) {
                return Result.error("Token无效");
            }
            Integer userId = (Integer) claims.get("userId");
            user.setId(userId);
            userService.updateProfile(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Update password
    @PutMapping("/password")
    public Result<Object> updatePassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                          @RequestBody Map<String, String> params) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null) {
                return Result.error("Token无效");
            }
            Integer userId = (Integer) claims.get("userId");
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            userService.updatePassword(userId, oldPassword, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Admin: Get merchant list
    @GetMapping("/admin/merchant-list")
    public Result<IPage<User>> getMerchantList(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) String username) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null) {
                return Result.error("Token无效");
            }
            String role = (String) claims.get("role");
            if (!"ADMIN".equals(role)) {
                return Result.error("无权限访问");
            }
            IPage<User> page = userService.getMerchantList(pageNum, pageSize, username);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Admin: Delete user
    @DeleteMapping("/admin/{id}")
    public Result<Object> deleteUser(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                      @PathVariable Integer id) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null) {
                return Result.error("Token无效");
            }
            String role = (String) claims.get("role");
            if (!"ADMIN".equals(role)) {
                return Result.error("无权限访问");
            }
            userService.deleteUser(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Admin: Update user
    @PutMapping("/admin/update")
    public Result<Object> updateUser(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                      @RequestBody User user) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null) {
                return Result.error("Token无效");
            }
            String role = (String) claims.get("role");
            if (!"ADMIN".equals(role)) {
                return Result.error("无权限访问");
            }
            userService.updateUserByAdmin(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Admin: Toggle user status (enable/disable)
    @PutMapping("/admin/status")
    public Result<Object> toggleUserStatus(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestBody Map<String, Object> params) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null) {
                return Result.error("Token无效");
            }
            String role = (String) claims.get("role");
            if (!"ADMIN".equals(role)) {
                return Result.error("无权限访问");
            }
            Integer userId = (Integer) params.get("userId");
            Integer status = (Integer) params.get("status");
            userService.toggleUserStatus(userId, status);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Admin: Get user's hotels and rooms
    @GetMapping("/admin/{userId}/hotels")
    public Result<Map<String, Object>> getUserHotels(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                      @PathVariable Integer userId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null) {
                return Result.error("Token无效");
            }
            String role = (String) claims.get("role");
            if (!"ADMIN".equals(role)) {
                return Result.error("无权限访问");
            }
            // 获取用户旗下的所有酒店
            QueryWrapper<Hotel> hotelWrapper = new QueryWrapper<>();
            hotelWrapper.eq("merchant_id", userId);
            List<Hotel> hotels = hotelService.list(hotelWrapper);
            
            // 获取每个酒店的房间
            List<Map<String, Object>> hotelList = hotels.stream().map(hotel -> {
                Map<String, Object> hotelMap = new HashMap<>();
                hotelMap.put("id", hotel.getId());
                hotelMap.put("name", hotel.getName());
                hotelMap.put("address", hotel.getAddress());
                hotelMap.put("city", hotel.getCity());
                hotelMap.put("starRating", hotel.getStarRating());
                hotelMap.put("status", hotel.getStatus());
                hotelMap.put("coverImage", hotel.getCoverImage());
                hotelMap.put("minPrice", hotel.getMinPrice());
                
                // 获取该酒店的房间
                QueryWrapper<RoomType> roomWrapper = new QueryWrapper<>();
                roomWrapper.eq("hotel_id", hotel.getId());
                List<RoomType> rooms = roomTypeService.list(roomWrapper);
                hotelMap.put("rooms", rooms);
                hotelMap.put("roomCount", rooms.size());
                
                return hotelMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("hotels", hotelList);
            result.put("hotelCount", hotels.size());
            result.put("totalRoomCount", hotelList.stream().mapToInt(h -> (Integer) h.get("roomCount")).sum());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Admin: Get consumer list
    @GetMapping("/admin/consumer-list")
    public Result<IPage<User>> getConsumerList(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String username) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null) {
                return Result.error("Token无效");
            }
            String role = (String) claims.get("role");
            if (!"ADMIN".equals(role)) {
                return Result.error("无权限访问");
            }
            IPage<User> page = userService.getConsumerList(pageNum, pageSize, username);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
