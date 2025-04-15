package com.example.myapplication.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 菜谱特征提取类
 * 包含结构化标签和非结构化分析
 */
public class RecipeFeature {
    private Map<String, RecipeData> recipeFeatures;
    
    public RecipeFeature() {
        this.recipeFeatures = new HashMap<>();
    }
    
    /**
     * 添加结构化标签
     * @param recipeId 菜谱ID
     * @param tags 标签字典，如{'cuisine': '川菜', 'taste': ['辣', '咸'], 'difficulty': '简单', 'time': '30分钟'}
     */
    public void addStructuredTags(String recipeId, Map<String, Object> tags) {
        if (!recipeFeatures.containsKey(recipeId)) {
            recipeFeatures.put(recipeId, new RecipeData());
        }
        
        // 将列表类型的标签转换为字典，方便后续计算相似度
        Map<String, Float> processedTags = new HashMap<>();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                processedTags.put(key + "_" + value, 1.0f);
            } else if (value instanceof String[]) {
                for (String item : (String[]) value) {
                    processedTags.put(key + "_" + item, 1.0f);
                }
            } else if (value instanceof Iterable) {
                for (Object item : (Iterable<?>) value) {
                    if (item instanceof String) {
                        processedTags.put(key + "_" + item, 1.0f);
                    }
                }
            }
        }
        
        recipeFeatures.get(recipeId).getTags().putAll(processedTags);
    }
    
    /**
     * 从文本中提取关键词
     * @param recipeId 菜谱ID
     * @param text 文本内容，如评论、描述等
     * @param keywords 提取的关键词及其权重
     */
    public void extractNlpKeywords(String recipeId, String text, Map<String, Float> keywords) {
        if (!recipeFeatures.containsKey(recipeId)) {
            recipeFeatures.put(recipeId, new RecipeData());
        }
        
        // 在Android中，我们不使用jieba分词，而是直接接收已处理的关键词
        recipeFeatures.get(recipeId).getNlpKeywords().putAll(keywords);
    }
    
    /**
     * 获取菜谱特征向量
     * @param recipeId 菜谱ID
     * @return 特征向量数据
     */
    public RecipeData getRecipeFeatureVector(String recipeId) {
        return recipeFeatures.getOrDefault(recipeId, new RecipeData());
    }
    
    /**
     * 菜谱数据类，包含标签和关键词
     */
    public static class RecipeData {
        private Map<String, Float> tags;
        private Map<String, Float> nlpKeywords;
        
        public RecipeData() {
            this.tags = new HashMap<>();
            this.nlpKeywords = new HashMap<>();
        }
        
        public Map<String, Float> getTags() {
            return tags;
        }
        
        public Map<String, Float> getNlpKeywords() {
            return nlpKeywords;
        }
    }
}