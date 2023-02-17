package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.SaleChanceMapper;
import com.xxxx.crm.enums.DevResult;
import com.xxxx.crm.enums.StateStatus;
import com.xxxx.crm.query.SaleChanceQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.PhoneUtil;
import com.xxxx.crm.vo.SaleChance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Service
public class SaleChanceService extends BaseService<SaleChance,Integer> {
    @Resource
    private SaleChanceMapper saleChanceMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateSaleChanceDevResult(Integer id, Integer devResult) {
        //判断id是否为空
        AssertUtil.isTrue( null == id,"待更新记录不存在！！");
        //通过id查询营销机会数据
        SaleChance saleChance = saleChanceMapper.selectByPrimaryKey(id);
        //判断对象是否为空
        AssertUtil.isTrue( null == saleChance,"待更新记录不存在");
        //设置开发状态
        saleChance.setDevResult(devResult);
        //执行更新操作，判断受影响的行数
        AssertUtil.isTrue(saleChanceMapper.updateByPrimaryKeySelective(saleChance) != 1,"开发状态更新失败");
    }

    /**
     * 分页查询
     * @param saleChanceQuery
     * @return
     */
    public Map<String,Object> querySaleChancesByParams(SaleChanceQuery saleChanceQuery){
        Map<String,Object> map = new HashMap<String,Object>();
        //开启分页
        PageHelper.startPage(saleChanceQuery.getPage(),saleChanceQuery.getLimit());
        //得到分页对象
        PageInfo<SaleChance> pageInfo = new PageInfo<SaleChance>(saleChanceMapper.selectByParams(saleChanceQuery));
        //设置map对象
        map.put("code",0);
        map.put("msg","");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());
        //设置好分页的列表
        return map;

    }

    /**
     * 添加操作
     * @param saleChance
     */
    public void saveSaleChance(SaleChance saleChance){
        /**
         * 1.参数校验
         *      customerName  客户名非空
         *      linkMan  非空
         *      linkPhone  非空 11位手机号
         * 2. 设置相关参数默认值
         *       state 默认未分配   如果选择分配人  state 为已分配状态
         *       assignTime 默认空   如果选择分配人  分配时间为系统当前时间
         *       devResult  默认未开发  如果选择分配人 devResult 为开发中 0-未开发  1-开发中 2-开发成功 3-开发失败
         *       isValid  默认有效(1-有效  0-无效)
         *       createDate  updateDate:默认系统当前时间
         * 3.执行添加 判断添加结果
         */
        //参数校验（非空判断） 通过封装方法完成
        checkParams(saleChance.getCustomerName(),saleChance.getLinkMan(),saleChance.getLinkPhone());
        //设置相关默认值
        //未选择分配人
        saleChance.setState(StateStatus.UNSTATE.getType());//枚举 未分配
        saleChance.setDevResult(DevResult.UNDEV.getStatus());// 未开发
        if (StringUtils.isNotBlank(saleChance.getAssignMan())){//如果有分配人
            saleChance.setState(StateStatus.STATED.getType());//已分配
            saleChance.setDevResult(DevResult.DEVING.getStatus());//开发中
            saleChance.setAssignTime(new Date());
        }
        saleChance.setIsValid(1);//默认状态为有效
        saleChance.setCreateDate(new Date());
        saleChance.setUpdateDate(new Date());
        //执行添加  判断添加结果
        AssertUtil.isTrue(insertSelective(saleChance)<1,"机会添加失败");
    }

    private void checkParams(String customerName, String linkMan, String linkPhone) {
        //判断客户名非空
        AssertUtil.isTrue(StringUtils.isBlank(customerName),"请输入用户名！！");
        AssertUtil.isTrue(StringUtils.isBlank(linkMan),"请输入联系人！！");
        AssertUtil.isTrue(StringUtils.isBlank(linkPhone),"请输入手机号！！");
        //判断手机号格式是否正确   注意取反
        AssertUtil.isTrue(!(PhoneUtil.isMobile(linkPhone)),"手机号格式不正确");

    }

    /**
     * 修改操作
     *
     * @param saleChance
     */
    public void updateSaleChance(SaleChance saleChance) {
        //根据id查询用户记录
        SaleChance temp = selectByPrimaryKey(saleChance.getId());
        //判断用户记录是否存在
        AssertUtil.isTrue(null == temp, "待更新记录不存在");
        //参数校验
        checkParams(temp.getCustomerName(), temp.getLinkMan(), temp.getLinkPhone());
        //设置相关参数
        saleChance.setUpdateDate(new Date());
        // 判断原始数据是否存在
        if (StringUtils.isBlank(temp.getAssignMan())) { // 不存在
            // 判断修改后的值是否存在
            if (!StringUtils.isBlank(saleChance.getAssignMan())) { // 修改前为空，修改后有值
                // assignTime指派时间  设置为系统当前时间
                saleChance.setAssignTime(new Date());
                // 分配状态    1=已分配
                saleChance.setState(StateStatus.STATED.getType());
                // 开发状态    1=开发中
                saleChance.setDevResult(DevResult.DEVING.getStatus());
            }
        } else { // 存在
            // 判断修改后的值是否存在
            if (StringUtils.isBlank(saleChance.getAssignMan())) { // 修改前有值，修改后无值
                // assignTime指派时间  设置为null
                saleChance.setAssignTime(null);
                // 分配状态    0=未分配
                saleChance.setState(StateStatus.UNSTATE.getType());
                // 开发状态    0=未开发
                saleChance.setDevResult(DevResult.UNDEV.getStatus());
            } else { // 修改前有值，修改后有值
                // 判断修改前后是否是同一个用户
                if (!saleChance.getAssignMan().equals(temp.getAssignMan())) {
                    // 更新指派时间
                    saleChance.setAssignTime(new Date());
                } else {
                    // 设置指派时间为修改前的时间
                    saleChance.setAssignTime(temp.getAssignTime());
                }
            }
        }

        /* 3. 执行更新操作，判断受影响的行数 */
        AssertUtil.isTrue(saleChanceMapper.updateByPrimaryKeySelective(saleChance) != 1, "更新营销机会失败！");

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteSaleChance(Integer[] ids){
        //判断id是否为空
        AssertUtil.isTrue(null == ids || ids.length < 1,"待删除记录不存在！！");
        //执行删除操作
        AssertUtil.isTrue(saleChanceMapper.deleteBatch(ids) != ids.length,"营销机会数据删除失败");
    }
}
