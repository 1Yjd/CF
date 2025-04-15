package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.RecipeFeature;
import com.example.myapplication.model.UserProfile;
import com.example.myapplication.recommender.CollaborativeFilteringRecommender;
import com.example.myapplication.recommender.ContentBasedRecommender;
import com.example.myapplication.recommender.HybridRecommender;
import com.example.myapplication.recommender.RecipeScore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView resultTextView;
    private Button recommendButton;
    
    // 推荐系统组件
    private UserProfile userProfileManager;
    private RecipeFeature recipeFeatureManager;
    private HybridRecommender hybridRecommender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化UI组件
        resultTextView = findViewById(R.id.resultTextView);
        recommendButton = findViewById(R.id.recommendButton);
        
        // 初始化推荐系统
        initRecommendationSystem();
        
        // 设置按钮点击事件
        recommendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRecommendations();
            }
        });
    }
    
    /**
     * 初始化推荐系统
     */
    private void initRecommendationSystem() {
        // 创建用户画像管理器
        userProfileManager = new UserProfile();
        
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
        recipeFeatureManager = new RecipeFeature();
        
        // 添加示例菜谱
        addSampleRecipes();
        
        // 创建基于内容的推荐器
        ContentBasedRecommender contentRecommender = new ContentBasedRecommender(userProfileManager, recipeFeatureManager);
        
        // 创建协同过滤推荐器
        CollaborativeFilteringRecommender cfRecommender = new CollaborativeFilteringRecommender(userProfileManager);
        
        // 创建混合推荐器
        hybridRecommender = new HybridRecommender(contentRecommender, cfRecommender);
        
        // 设置混合权重
        hybridRecommender.setWeights(0.4f, 0.3f, 0.3f);
    }
    
    /**
     * 添加示例菜谱
     */
    private void addSampleRecipes() {
        // 添加菜谱1
        Map<String, Object> recipe1Tags = new HashMap<>();
        recipe1Tags.put("cuisine", "川菜");
        recipe1Tags.put("taste", new String[]{"辣", "咸"});
        recipe1Tags.put("difficulty", "简单");
        recipe1Tags.put("time", "30分钟");
        recipe1Tags.put("season", "夏季");
        
        recipeFeatureManager.addStructuredTags("recipe1", recipe1Tags);
        
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
        
        // 添加菜谱2
        Map<String, Object> recipe2Tags = new HashMap<>();
        recipe2Tags.put("cuisine", "粤菜");
        recipe2Tags.put("taste", new String[]{"甜", "鲜"});
        recipe2Tags.put("difficulty", "中等");
        recipe2Tags.put("time", "45分钟");
        recipe2Tags.put("season", "四季");
        
        recipeFeatureManager.addStructuredTags("recipe2", recipe2Tags);
        
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
        
        // 添加菜谱3
        Map<String, Object> recipe3Tags = new HashMap<>();
        recipe3Tags.put("cuisine", "湘菜");
        recipe3Tags.put("taste", new String[]{"辣", "香"});
        recipe3Tags.put("difficulty", "中等");
        recipe3Tags.put("time", "40分钟");
        recipe3Tags.put("season", "四季");
        
        recipeFeatureManager.addStructuredTags("recipe3", recipe3Tags);
        
        Map<String, Float> recipe3Keywords = new HashMap<>();
        recipe3Keywords.put("香辣", 0.9f);
        recipe3Keywords.put("下饭", 0.8f);
        recipe3Keywords.put("经典", 0.7f);
        
        recipeFeatureManager.extractNlpKeywords(
            "recipe3",
            "湘菜以香辣著称，这道菜是经典下饭菜，香气扑鼻。",
            recipe3Keywords
        );
    }
    
    /**
     * 生成推荐结果
     */
    private void generateRecommendations() {
        // 候选菜谱列表
        List<String> candidateRecipes = Arrays.asList("recipe1", "recipe2", "recipe3");
        
        // 获取推荐结果
        List<RecipeScore> recommendations = hybridRecommender.recommend(
            "user1",
            candidateRecipes,
            10,
            false
        );
        
        // 显示推荐结果
        StringBuilder resultBuilder = new StringBuilder("推荐结果：\n\n");
        for (RecipeScore rec : recommendations) {
            resultBuilder.append("菜谱ID: ").append(rec.getRecipeId())
                    .append(", 推荐分数: ").append(String.format("%.2f", rec.getScore()))
                    .append("\n");
        }
        
        resultTextView.setText(resultBuilder.toString());
        Toast.makeText(this, "已生成推荐结果", Toast.LENGTH_SHORT).show();
    }
}