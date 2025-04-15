package com.example.myapplication.recommender;

import com.example.myapplication.model.RecipeFeature;
import com.example.myapplication.model.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推荐系统示例使用类
 * 展示如何使用推荐系统的各个组件
 */
public class RecommendationExample {

    /**
     * 示例使用方法
     * 展示如何初始化和使用推荐系统
     */
    public static void exampleUsage() {
        // 创建用户画像管理器
        UserProfile userProfileManager = new UserProfile();
        
        // 添加用户静态偏好
        Map<String, List<String>> preferences = new HashMap<>();
        preferences.put("cuisine", Arrays.asList("川菜", "粤菜"));
        preferences.put("taste", Arrays.asList("辣", "咸"));
        preferences.put("cooking_method", Arrays.asList("炒", "蒸"));
        
        userProfileManager.createStaticProfile("user1", preferences);
        
        // 添加用户动态行为
        Map<String, Float> recipeTags = new HashMap<>();
        recipeTags.put("cuisine_川菜", 1.0f);
        recipeTags.put("taste_辣", 1.0f);
        
        userProfileManager.updateDynamicProfile(
            "user1",
            "recipe1",
            "collect",
            1.0f,
            recipeTags
        );
        
        // 添加上下文信息
        Map<String, String> contextInfo = new HashMap<>();
        contextInfo.put("season", "夏季");
        contextInfo.put("location", "北京");
        
        userProfileManager.updateContextInfo("user1", contextInfo);
        
        // 创建菜谱特征管理器
        RecipeFeature recipeFeatureManager = new RecipeFeature();
        
        // 添加菜谱结构化标签
        Map<String, Object> recipe1Tags = new HashMap<>();
        recipe1Tags.put("cuisine", "川菜");
        recipe1Tags.put("taste", new String[]{"辣", "咸"});
        recipe1Tags.put("difficulty", "简单");
        recipe1Tags.put("time", "30分钟");
        recipe1Tags.put("season", "夏季");
        
        recipeFeatureManager.addStructuredTags("recipe1", recipe1Tags);
        
        Map<String, Object> recipe2Tags = new HashMap<>();
        recipe2Tags.put("cuisine", "粤菜");
        recipe2Tags.put("taste", new String[]{"甜", "鲜"});
        recipe2Tags.put("difficulty", "中等");
        recipe2Tags.put("time", "45分钟");
        recipe2Tags.put("season", "四季");
        
        recipeFeatureManager.addStructuredTags("recipe2", recipe2Tags);
        
        // 提取菜谱NLP关键词
        Map<String, Float> recipe1Keywords = new HashMap<>();
        recipe1Keywords.put("麻辣", 0.9f);
        recipe1Keywords.put("开胃", 0.8f);
        recipe1Keywords.put("简单", 0.7f);
        recipe1Keywords.put("家庭", 0.6f);
        
        recipeFeatureManager.extractNlpKeywords(
            "recipe1",
            "这道川菜麻辣鲜香，非常适合夏天开胃，做法简单，很适合家庭烹饪。",
            recipe1Keywords
        );
        
        Map<String, Float> recipe2Keywords = new HashMap<>();
        recipe2Keywords.put("清淡", 0.9f);
        recipe2Keywords.put("营养", 0.8f);
        recipe2Keywords.put("鲜甜", 0.7f);
        recipe2Keywords.put("适合", 0.6f);
        
        recipeFeatureManager.extractNlpKeywords(
            "recipe2",
            "粤菜清淡爽口，营养丰富，这道菜鲜甜可口，老少皆宜。",
            recipe2Keywords
        );
        
        // 创建基于内容的推荐器
        ContentBasedRecommender contentRecommender = new ContentBasedRecommender(userProfileManager, recipeFeatureManager);
        
        // 创建协同过滤推荐器
        CollaborativeFilteringRecommender cfRecommender = new CollaborativeFilteringRecommender(userProfileManager);
        
        // 创建混合推荐器
        HybridRecommender hybridRecommender = new HybridRecommender(contentRecommender, cfRecommender);
        
        // 设置混合权重
        hybridRecommender.setWeights(0.4f, 0.3f, 0.3f);
        
        // 获取推荐结果
        List<String> candidateRecipes = Arrays.asList("recipe1", "recipe2");
        List<RecipeScore> recommendations = hybridRecommender.recommend(
            "user1",
            candidateRecipes,
            10,
            true
        );
        
        // 打印推荐结果
        System.out.println("推荐结果：");
        for (RecipeScore rec : recommendations) {
            System.out.println("菜谱ID: " + rec.getRecipeId() + ", 推荐分数: " + rec.getScore());
        }
    }
}