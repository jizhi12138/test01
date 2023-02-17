package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.crm.base.BaseQuery;
import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.UserMapper;
import com.xxxx.crm.dao.UserRoleMapper;
import com.xxxx.crm.model.UserModel;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.Md5Util;
import com.xxxx.crm.utils.PhoneUtil;
import com.xxxx.crm.utils.UserIDBase64;
import com.xxxx.crm.vo.User;
import com.xxxx.crm.vo.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService extends BaseService<User,Integer> {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    /**
     用户登录
        1. 参数判断，判断用户姓名、用户密码非空
            如果参数为空，抛出异常（异常被控制层捕获并处理）
        2. 调用数据访问层，通过用户名查询用户记录，返回用户对象
        3. 判断用户对象是否为空
            如果对象为空，抛出异常（异常被控制层捕获并处理）
        4. 判断密码是否正确，比较客户端传递的用户密码与数据库中查询的用户对象中的用户密码
             如果密码不相等，抛出异常（异常被控制层捕获并处理）
        5. 如果密码正确，登录成功
     * @param userName
     * @param userPwd
     */
    public UserModel userLogin(String userName, String userPwd){
        //参数判断，判断用户姓名，用户密码非空
        checkLoginParams(userName,userPwd);
        //调用数据访问层，通过用户名查询用户记录，返回用户对象
        User user = userMapper.queryUserByUserName(userName);
        //判断用户对象是否为空
        AssertUtil.isTrue(user == null,"用户姓名不存在");
        //判断密码是否正确
        checkUserPwd(userPwd,user.getUserPwd());
        //返回构建用户对象
        return buildUserInfo(user);

    }

    private UserModel buildUserInfo(User user) {
        UserModel userModel = new UserModel();
        // userModel.setUserId(user.getId());
        // 设置加密的用户ID
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        return userModel;
    }

    private void checkUserPwd(String userPwd, String pwd) {
        //将客户端传递的密码加密
        userPwd = Md5Util.encode(userPwd);
        //判断密码是否相等
        AssertUtil.isTrue(!userPwd.equals(pwd),"用户密码不正确！！");
    }

    private void checkLoginParams(String userName, String userPwd) {
        //验证用户姓名
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户姓名不能为空！");
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"用户密码不能为空！");
    }

    /**
     * 修改操作
     * @param userId
     * @param oldPwd
     * @param newPwd
     * @param repeatPwd
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updatePassWord(Integer userId, String oldPwd, String newPwd, String repeatPwd){
        //判断用户ID查询用户记录，返回用户对象
        User user = userMapper.selectByPrimaryKey(userId);
        //判断用户记录是否存在
        AssertUtil.isTrue(null == user,"待更新记录不存在！");
        //参数校验
        checkPasswordParams(user, oldPwd, newPwd, repeatPwd);
        //设置新密码
        user.setUserPwd(Md5Util.encode(newPwd));
        //执行更新
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user) < 1, "修改密码失败！");
    }

    /**
     * 修改密码的参数校验
     * @param user
     * @param oldPwd
     * @param newPwd
     * @param repeatPwd
     */
    private void checkPasswordParams(User user, String oldPwd, String newPwd, String repeatPwd) {
        // 判断原始密码是否为空
        AssertUtil.isTrue(StringUtils.isBlank(oldPwd),"用户密码不能为空");
        // 判断原始密码是否正确（查询的用户对象中的用户密码是否原始密码一致）
        AssertUtil.isTrue(!user.getUserPwd().equals(Md5Util.encode(oldPwd)),"原始密码不正确！！");
        // 判断新密码是否为空
        AssertUtil.isTrue(StringUtils.isBlank(newPwd),"新密码不能为空");
        // 判断新密码是否与原始密码一致 （不允许新密码与原始密码）
        AssertUtil.isTrue(oldPwd.equals(newPwd),"新密码不能与原始密码相同");
        // 判断确认密码是否为空
        AssertUtil.isTrue(StringUtils.isBlank(repeatPwd),"确认密码不能为空");
        // 判断确认密码是否与新密码一致
        AssertUtil.isTrue(!newPwd.equals(repeatPwd),"确认密码与新密码不一致");
    }

    public List<Map<String,Object>> queryAllSales(){
        return userMapper.queryAllSales();
    }

    public Map<String,Object> queryByParamsForTable(BaseQuery baseQuery){
        Map<String,Object> result = new HashMap<>();
        PageHelper.startPage(baseQuery.getPage(), baseQuery.getLimit());
        PageInfo<User> pageInfo = new PageInfo<>(userMapper.selectByParams(baseQuery));
        result.put("code",0);
        result.put("msg", "");
        result.put("count", pageInfo.getTotal());
        result.put("data", pageInfo.getList());
        return result;
    }
    /**
 * 添加用户
 *  1. 参数校验
 *      用户名userName     非空，唯一性
 *      邮箱email          非空
 *      手机号phone        非空，格式正确
 *  2. 设置参数的默认值
 *      isValid           1
 *      createDate        系统当前时间
 *      updateDate        系统当前时间
 *      默认密码            123456 -> md5加密
 *  3. 执行添加操作，判断受影响的行数
 *
 *
 * 乐字节：专注线上IT培训
 * 答疑老师微信：lezijie
 * @param user
 * @ret
 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addUser(User user){
        //参数校验
        checkUserParams(user.getUserName(), user.getEmail(), user.getPhone(), null);
        //设置参数的默认值
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        //设置密码默认值
        user.setUserPwd(Md5Util.encode("123456"));
        //执行添加操作，判断作用行数
        AssertUtil.isTrue(userMapper.insertSelective(user) < 1, "用户添加失败！");

        //用户角色关联
        relationUserRole(user.getId(), user.getRoleIds());


    }

    private void relationUserRole(Integer userId, String roleIds) {
        //通过用户id查询角色记录
        Integer count = userRoleMapper.countUserRoleByUserId(userId);
        //判断用户记录是否存在
        if (count > 0){
            //如果存在，删除该记录
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色分配失败！");
        }
        //判断角色id是否存在，如果存在，添加该记录
        if (StringUtils.isNotBlank(roleIds)){
            //将用户角色数据设置到集合中，执行批量添加操作
            List<UserRole> list = new ArrayList<>();
            //分割数组，获得每一个角色id
            String[] ids = roleIds.split(",");
            //遍历数组，将对应的用户角色对象设置到集合内
            for(String roleId : ids){
                UserRole userRole = new UserRole();
                userRole.setRoleId(Integer.parseInt(roleId));
                userRole.setUserId(userId);
                userRole.setCreateDate(new Date());
                userRole.setUpdateDate(new Date());
                list.add(userRole);
            }
            //皮力量添加用户角色记录
            AssertUtil.isTrue(userRoleMapper.insertBatch(list) != list.size(), "用户角色分配失败！");
        }


    }

    /**
     * 更新用户
     *  1. 参数校验
     *      判断用户ID是否为空，且数据存在
     *      用户名userName     非空，唯一性
     *      邮箱email          非空
     *      手机号phone        非空，格式正确
     *  2. 设置参数的默认值
     *      updateDate        系统当前时间
     *  3. 执行更新操作，判断受影响的行数
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param user
     * @return void
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUser(User user) {
        // 判断用户ID是否为空，且数据存在
        AssertUtil.isTrue(null == user.getId(), "待更新记录不存在！");
        // 通过id查询数据
        User temp = userMapper.selectByPrimaryKey(user.getId());
        // 判断是否存在
        AssertUtil.isTrue(null == temp, "待更新记录不存在！");
        // 参数校验
        checkUserParams(user.getUserName(), user.getEmail(), user.getPhone(), user.getId());

        // 设置默认值
        user.setUpdateDate(new Date());

        // 执行更新操作，判断受影响的行数
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user) != 1, "用户更新失败！");

        //用户角色关联
        relationUserRole(user.getId(), user.getRoleIds());
    }


    private void checkUserParams(String userName, String email, String phone, Integer userId) {
//        用户名userName     非空，唯一性
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户姓名不能为空");
        //判断用户名的唯一性
        User temp = userMapper.queryUserByUserName(userName);
        AssertUtil.isTrue(null != temp && !(temp.getId().equals(userId)),"用户名已存在，请重新输入！");
//        邮箱email          非空
        AssertUtil.isTrue(StringUtils.isBlank(email),"用户邮箱不能为空");
//        手机号phone        非空，格式正确
        AssertUtil.isTrue(StringUtils.isBlank(phone),"用户手机号不能为空");
        AssertUtil.isTrue(!PhoneUtil.isMobile(phone),"用户手机号格式不正确");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteByIds(Integer[] ids){
        //判断ids是否为空
        AssertUtil.isTrue(ids == null || ids.length < 0 ,"待删除记录不存在");
        //执行删除操作，返回待影响的行数
        AssertUtil.isTrue(userMapper.deleteBatch(ids) != ids.length,"记录删除失败");
        for (Integer id : ids){
            Integer count = userRoleMapper.countUserRoleByUserId(id);
            if (count > 0){
                AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(id) != count,"用户记录删除失败！");
            }
        }
    }
}
