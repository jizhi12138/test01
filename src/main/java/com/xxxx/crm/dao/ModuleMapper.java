package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.model.TreeModel;
import com.xxxx.crm.vo.Module;
import org.apache.ibatis.annotations.Param;


import java.util.List;

public interface ModuleMapper extends BaseMapper<Module,Integer> {
    public List<TreeModel> queryAllModules();
    public List<Module> queryModuleList();

    Module queryModuleByGradeAndModuleName(@Param("grade")Integer grade, @Param("moduleName")String moduleName);

    Module queryModuleByGradeAndUrl(@Param("grade")Integer grade,@Param("url") String url);

    Module queryModuleByOptValue(String optValue);

    Integer queryModuleByParentId(Integer id);

}