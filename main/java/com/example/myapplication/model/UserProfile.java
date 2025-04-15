package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户画像构建类
 * 包含静态属性和动态行为分析
 */
public class UserProfile {
    private Map<String, UserData> userProfiles;
    private Map<String, Float> behaviorWeights;
    
    public UserProfile() {
        this.userProfiles = new HashMap<>();
        this.behaviorWeights = new HashMap<>();
        
        // 初始化行为权重
        behaviorWeights.put("browse", 1.0f);   // 浏览权重
        behaviorWeights.put("collect", 3.0f);  // 收藏权重
        behaviorWeights.put("rate", 4.0f);     // 评分权重
        behaviorWeights.put("cook", 5.0f);     // 制作记录权重
    }
    
    /**
     * 创建用户静态画像
     * @param userId 用户ID
     * @param preferences 用户偏好，如{'cuisine': ['川菜', '粤菜'], 'taste': ['辣', '咸'], 'cooking_method': ['炒', '蒸']}
     */
    public void createStaticProfile(String userId, Map<String, List<String>> preferences) {
        if (!userProfiles.containsKey(userId)) {
            userProfiles.put(userId, new UserData());
        }
        
        userProfiles.get(userId).setStaticPreferences(preferences);
    }
    
    /**
     * 更新用户动态画像
     * @param userId 用户ID
     * @param recipeId 菜谱ID
     * @param behaviorType 行为类型，如'browse', 'collect', 'rate', 'cook'
     * @param value 行为值，如评分为1-5
     * @param recipeTags 菜谱标签，用于更新用户兴趣标签
     */
    public void updateDynamicProfile(String userId, String recipeId, String behaviorType, float value, Map<String, Float> recipeTags) {
        if (!userProfiles.containsKey(userId)) {
            userProfiles.put(userId, new UserData());
        }
        
        UserData userData = userProfiles.get(userId);
        
        // 更新行为记录
        float weight = behaviorWeights.getOrDefault(behaviorType, 1.0f);
        Map<String, Float> dynamicBehavior = userData.getDynamicBehavior();
        
        // 更新或添加菜谱评分
        float currentScore = dynamicBehavior.getOrDefault(recipeId, 0.0f);
        dynamicBehavior.put(recipeId, currentScore + weight * value);
        
        // 更新用户兴趣标签
        if (recipeTags != null) {
            Map<String, Float> interests = userData.getInterests();
            
            for (Map.Entry<String, Float> entry : recipeTags.entrySet()) {
                String tag = entry.getKey();
                float tagWeight = entry.getValue();
                
                float currentInterest = interests.getOrDefault(tag, 0.0f);
                interests.put(tag, currentInterest + weight * value * tagWeight);
            }
        }
    }
    
    /**
     * 更新上下文信息
     * @param userId 用户ID
     * @param contextInfo 上下文信息，如{'time': '春节', 'location': '北京', 'season': '冬季'}
     */
    public void updateContextInfo(String userId, Map<String, String> contextInfo) {
        if (!userProfiles.containsKey(userId)) {
            userProfiles.put(userId, new UserData());
        }
        
        userProfiles.get(userId).getContextInfo().putAll(contextInfo);
    }
    
    /**
     * 获取用户画像
     * @param userId 用户ID
     * @return 用户画像数据
     */
    public UserData getUserProfile(String userId) {
        return userProfiles.getOrDefault(userId, new UserData());
    }
    
    /**
     * 获取所有用户ID
     * @return 用户ID列表
     */
    public List<String> getAllUserIds() {
        return new ArrayList<>(userProfiles.keySet());
    }
    
    /**
     * 用户数据类，包含静态偏好、动态行为和上下文信息
     */
    public static class UserData {
        private Map<String, List<String>> staticPreferences;
        private Map<String, Float> dynamicBehavior;
        private Map<String, String> contextInfo;
        private Map<String, Float> interests;
        
        public UserData() {
            this.staticPreferences = new HashMap<>();
            this.dynamicBehavior = new HashMap<>();
            this.contextInfo = new HashMap<>();
            this.interests = new HashMap<>();
        }
        
        public Map<String, List<String>> getStaticPreferences() {
            return staticPreferences;
        }
        
        public void setStaticPreferences(Map<String, List<String>> staticPreferences) {
            this.staticPreferences = staticPreferences;
        }
        
        public Map<String, Float> getDynamicBehavior() {
            return dynamicBehavior;
        }
        
        public Map<String, String> getContextInfo() {
            return contextInfo;
        }
        
        public Map<String, Float> getInterests() {
            return interests;
        }
    }
}