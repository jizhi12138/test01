package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.vo.Permission;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission,Integer> {

    //根据id查询
    Integer countPermissionByRoleId(Integer roleId);
    //删除
    void deletePermissionByRoleId(Integer roleId);

    List<Integer> queryRoleHasModuleIdsByRoleId(Integer roleId);

    List<String> queryUserHasRoleHasPermissionByUserId(Integer userId);


    Integer countPermissionByModuleId(Integer id);

    Integer deletePermissionByModuleId(Integer id);

}