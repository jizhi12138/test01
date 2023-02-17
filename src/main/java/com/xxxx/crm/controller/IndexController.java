package com.xxxx.crm.controller;

import com.xxxx.crm.base.BaseController;
import com.xxxx.crm.service.PermissionService;
import com.xxxx.crm.service.UserService;
import com.xxxx.crm.utils.LoginUserUtil;
import com.xxxx.crm.vo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController extends BaseController {
    @Resource
    private UserService userService;
    @Resource
    private PermissionService permissionService;
    /**
     * 系统登录页
     * @return
     */
    @RequestMapping("index")
    public String index(){
        return "index";
    }

    // 系统界面欢迎页
    @RequestMapping("welcome")
    public String welcome(){
        return "welcome";
    }
    /**
     * 后端管理主页面
     * @return
     */
    @RequestMapping("main")
    public String main(HttpServletRequest request) {
        // 通过⼯具类，从cookie中获取userId
        Integer userId = LoginUserUtil.releaseUserIdFromCookie(request);
        // 调⽤对应Service层的⽅法，通过userId主键查询⽤户对象
        User user = userService.selectByPrimaryKey(userId);
        // 将⽤户对象设置到request作⽤域中
        request.getSession().setAttribute("user", user);
        //通过当前登录用户的id查询当前登录用户拥有的资源列表  查询相应的授权码
        List<String> permissions = permissionService.queryUserHasRoleHasPermissionByUserId(userId);
        //设置到请求域中
        request.getSession().setAttribute("permissions",permissions);
        return "main";
    }
}
