package com.example.myapplication.recommender;

import com.example.myapplication.model.RecipeFeature;
import com.example.myapplication.model.UserProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 基于内容的推荐算法
 * 主要用于解决冷启动问题
 */
public class ContentBasedRecommender {
    private UserProfile userProfile;
    private RecipeFeature recipeFeature;
    
    public ContentBasedRecommender(UserProfile userProfile, RecipeFeature recipeFeature) {
        this.userProfile = userProfile;
        this.recipeFeature = recipeFeature;
    }
    
    /**
     * 计算用户与菜谱的相似度
     * @param userId 用户ID
     * @param recipeId 菜谱ID
     * @return 相似度分数
     */
    public float calculateSimilarity(String userId, String recipeId) {
        UserProfile.UserData userData = userProfile.getUserProfile(userId);
        RecipeFeature.RecipeData recipeData = recipeFeature.getRecipeFeatureVector(recipeId);
        
        // 初始化相似度分数
        float similarityScore = 0.0f;
        
        // 计算静态偏好与菜谱标签的匹配度
        Map<String, List<String>> staticPreferences = userData.getStaticPreferences();
        if (staticPreferences != null && !staticPreferences.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : staticPreferences.entrySet()) {
                String prefType = entry.getKey();
                List<String> prefValues = entry.getValue();
                
                for (String value : prefValues) {
                    String tagKey = prefType + "_" + value;
                    if (recipeData.getTags().containsKey(tagKey)) {
                        similarityScore += 1.0f;
                    }
                }
            }
        }
        
        // 计算用户兴趣与菜谱标签的匹配度
        Map<String, Float> interests = userData.getInterests();
        if (interests != null && !interests.isEmpty()) {
            for (Map.Entry<String, Float> entry : interests.entrySet()) {
                String interest = entry.getKey();
                float weight = entry.getValue();
                
                // 考虑标签匹配
                if (recipeData.getTags().containsKey(interest)) {
                    similarityScore += weight * recipeData.getTags().get(interest);
                }
                
                // 考虑NLP关键词匹配
                if (recipeData.getNlpKeywords().containsKey(interest)) {
                    // NLP关键词权重较低
                    similarityScore += weight * recipeData.getNlpKeywords().get(interest) * 0.5f;
                }
            }
        }
        
        // 考虑上下文信息
        Map<String, String> contextInfo = userData.getContextInfo();
        if (contextInfo != null && !contextInfo.isEmpty()) {
            for (Map.Entry<String, String> entry : contextInfo.entrySet()) {
                String contextType = entry.getKey();
                String contextValue = entry.getValue();
                
                String contextTag = contextType + "_" + contextValue;
                if (recipeData.getTags().containsKey(contextTag)) {
                    // 上下文匹配给予更高权重
                    similarityScore += 2.0f;
                }
            }
        }
        
        return similarityScore;
    }
    
    /**
     * 为用户推荐菜谱
     * @param userId 用户ID
     * @param recipeIds 候选菜谱ID列表
     * @param topN 推荐数量
     * @return 推荐菜谱ID列表及其相似度分数
     */
    public List<RecipeScore> recommend(String userId, List<String> recipeIds, int topN) {
        // 计算用户与每个菜谱的相似度
        List<RecipeScore> similarities = new ArrayList<>();
        for (String recipeId : recipeIds) {
            float score = calculateSimilarity(userId, recipeId);
            similarities.add(new RecipeScore(recipeId, score));
        }
        
        // 按相似度降序排序
        Collections.sort(similarities, new Comparator<RecipeScore>() {
            @Override
            public int compare(RecipeScore o1, RecipeScore o2) {
                return Float.compare(o2.getScore(), o1.getScore());
            }
        });
        
        // 返回top_n个推荐
        if (similarities.size() > topN) {
            return similarities.subList(0, topN);
        } else {
            return similarities;
        }
    }
}