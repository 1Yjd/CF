package com.example.myapplication.recommender;

/**
 * 菜谱推荐分数类
 * 用于存储菜谱ID和对应的推荐分数
 */
public class RecipeScore {
    private String recipeId;
    private float score;
    
    public RecipeScore(String recipeId, float score) {
        this.recipeId = recipeId;
        this.score = score;
    }
    
    public String getRecipeId() {
        return recipeId;
    }
    
    public float getScore() {
        return score;
    }
    
    @Override
    public String toString() {
        return "RecipeScore{" +
                "recipeId='" + recipeId + '\'' +
                ", score=" + score +
                '}';
    }
}