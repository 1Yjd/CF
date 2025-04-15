package com.example.myapplication.recommender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 混合推荐算法
 * 结合基于内容的推荐和协同过滤推荐
 */
public class HybridRecommender {
    private ContentBasedRecommender contentRecommender;
    private CollaborativeFilteringRecommender cfRecommender;
    private Map<String, Float> weights;
    
    public HybridRecommender(ContentBasedRecommender contentRecommender, CollaborativeFilteringRecommender cfRecommender) {
        this.contentRecommender = contentRecommender;
        this.cfRecommender = cfRecommender;
        this.weights = new HashMap<>();
        
        // 默认权重设置
        weights.put("content", 0.4f);  // 基于内容的推荐权重
        weights.put("user_cf", 0.3f);  // 基于用户的协同过滤权重
        weights.put("item_cf", 0.3f);  // 基于物品的协同过滤权重
    }
    
    /**
     * 设置各推荐算法的权重
     * @param contentWeight 基于内容的推荐权重
     * @param userCfWeight 基于用户的协同过滤权重
     * @param itemCfWeight 基于物品的协同过滤权重
     */
    public void setWeights(float contentWeight, float userCfWeight, float itemCfWeight) {
        float total = contentWeight + userCfWeight + itemCfWeight;
        weights.put("content", contentWeight / total);
        weights.put("user_cf", userCfWeight / total);
        weights.put("item_cf", itemCfWeight / total);
    }
    
    /**
     * 混合推荐
     * @param userId 用户ID
     * @param recipeIds 候选菜谱ID列表
     * @param topN 推荐数量
     * @param isNewUser 是否为新用户
     * @return 推荐菜谱ID列表及其综合评分
     */
    public List<RecipeScore> recommend(String userId, List<String> recipeIds, int topN, boolean isNewUser) {
        // 根据用户是否为新用户调整权重
        Map<String, Float> tempWeights = new HashMap<>();
        if (isNewUser) {
            // 新用户更依赖基于内容的推荐
            tempWeights.put("content", 0.8f);
            tempWeights.put("user_cf", 0.1f);
            tempWeights.put("item_cf", 0.1f);
        } else {
            tempWeights = weights;
        }
        
        // 获取各推荐算法的结果
        List<RecipeScore> contentRecs = contentRecommender.recommend(userId, recipeIds, topN * 2);
        
        // 协同过滤可能需要先构建矩阵
        if (cfRecommender.getUserItemMatrix() == null) {
            cfRecommender.buildMatrices();
        }
        
        List<RecipeScore> userCfRecs = cfRecommender.userBasedRecommend(userId, topN * 2, 20);
        List<RecipeScore> itemCfRecs = cfRecommender.itemBasedRecommend(userId, topN * 2);
        
        // 合并推荐结果
        Map<String, Float> recipeScores = new HashMap<>();
        
        // 添加基于内容的推荐分数
        for (RecipeScore rec : contentRecs) {
            recipeScores.put(rec.getRecipeId(), rec.getScore() * tempWeights.get("content"));
        }
        
        // 添加基于用户的协同过滤推荐分数
        for (RecipeScore rec : userCfRecs) {
            float currentScore = recipeScores.getOrDefault(rec.getRecipeId(), 0.0f);
            recipeScores.put(rec.getRecipeId(), currentScore + rec.getScore() * tempWeights.get("user_cf"));
        }
        
        // 添加基于物品的协同过滤推荐分数
        for (RecipeScore rec : itemCfRecs) {
            float currentScore = recipeScores.getOrDefault(rec.getRecipeId(), 0.0f);
            recipeScores.put(rec.getRecipeId(), currentScore + rec.getScore() * tempWeights.get("item_cf"));
        }
        
        // 转换为列表并排序
        List<RecipeScore> mergedRecs = new ArrayList<>();
        for (Map.Entry<String, Float> entry : recipeScores.entrySet()) {
            mergedRecs.add(new RecipeScore(entry.getKey(), entry.getValue()));
        }
        
        Collections.sort(mergedRecs, new Comparator<RecipeScore>() {
            @Override
            public int compare(RecipeScore o1, RecipeScore o2) {
                return Float.compare(o2.getScore(), o1.getScore());
            }
        });
        
        // 返回top_n个推荐
        if (mergedRecs.size() > topN) {
            return mergedRecs.subList(0, topN);
        } else {
            return mergedRecs;
        }
    }
}