package com.example.myapplication.recommender;

import com.example.myapplication.model.UserProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 协同过滤推荐算法
 * 包括用户协同和物品协同
 */
public class CollaborativeFilteringRecommender {
    private UserProfile userProfile;
    private float[][] userItemMatrix;
    private float[][] itemUserMatrix;
    private float[][] userSimilarityMatrix;
    private float[][] itemSimilarityMatrix;
    private Map<String, Integer> userIndex;
    private Map<String, Integer> itemIndex;
    private Map<Integer, String> indexUser;
    private Map<Integer, String> indexItem;

    public CollaborativeFilteringRecommender(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.userItemMatrix = null;
        this.itemUserMatrix = null;
        this.userSimilarityMatrix = null;
        this.itemSimilarityMatrix = null;
        this.userIndex = new HashMap<>();
        this.itemIndex = new HashMap<>();
        this.indexUser = new HashMap<>();
        this.indexItem = new HashMap<>();
    }

    /**
     * 构建用户-物品矩阵和物品-用户矩阵
     */
    public void buildMatrices() {
        // 获取所有用户和物品
        List<String> users = new ArrayList<>();
        List<String> items = new ArrayList<>();
        Map<String, Map<String, Float>> userItemData = new HashMap<>();

        // 收集所有用户和物品ID
        for (String userId : userProfile.getAllUserIds()) {
            users.add(userId);
            UserProfile.UserData userData = userProfile.getUserProfile(userId);
            Map<String, Float> dynamicBehavior = userData.getDynamicBehavior();

            userItemData.put(userId, dynamicBehavior);
            for (String itemId : dynamicBehavior.keySet()) {
                if (!items.contains(itemId)) {
                    items.add(itemId);
                }
            }
        }

        // 构建索引映射
        for (int i = 0; i < users.size(); i++) {
            userIndex.put(users.get(i), i);
            indexUser.put(i, users.get(i));
        }

        for (int i = 0; i < items.size(); i++) {
            itemIndex.put(items.get(i), i);
            indexItem.put(i, items.get(i));
        }

        // 初始化矩阵
        userItemMatrix = new float[users.size()][items.size()];
        itemUserMatrix = new float[items.size()][users.size()];

        // 填充矩阵
        for (int i = 0; i < users.size(); i++) {
            String userId = users.get(i);
            Map<String, Float> userBehaviors = userItemData.get(userId);

            for (int j = 0; j < items.size(); j++) {
                String itemId = items.get(j);
                float score = userBehaviors.getOrDefault(itemId, 0.0f);
                userItemMatrix[i][j] = score;
                itemUserMatrix[j][i] = score;
            }
        }
    }

    /**
     * 计算用户相似度矩阵
     * 使用余弦相似度
     */
    public void calculateUserSimilarity() {
        int userCount = userItemMatrix.length;
        userSimilarityMatrix = new float[userCount][userCount];

        // 计算用户向量的范数
        float[] userNorms = new float[userCount];
        for (int i = 0; i < userCount; i++) {
            float sum = 0;
            for (int j = 0; j < userItemMatrix[i].length; j++) {
                sum += userItemMatrix[i][j] * userItemMatrix[i][j];
            }
            userNorms[i] = (float) Math.sqrt(sum) + 0.000001f; // 避免除零错误
        }

        // 计算余弦相似度
        for (int i = 0; i < userCount; i++) {
            for (int j = 0; j < userCount; j++) {
                float dotProduct = 0;
                for (int k = 0; k < userItemMatrix[i].length; k++) {
                    dotProduct += userItemMatrix[i][k] * userItemMatrix[j][k];
                }
                userSimilarityMatrix[i][j] = dotProduct / (userNorms[i] * userNorms[j]);
            }
        }
    }

    /**
     * 计算物品相似度矩阵
     * 使用余弦相似度
     */
    public void calculateItemSimilarity() {
        int itemCount = itemUserMatrix.length;
        itemSimilarityMatrix = new float[itemCount][itemCount];

        // 计算物品向量的范数
        float[] itemNorms = new float[itemCount];
        for (int i = 0; i < itemCount; i++) {
            float sum = 0;
            for (int j = 0; j < itemUserMatrix[i].length; j++) {
                sum += itemUserMatrix[i][j] * itemUserMatrix[i][j];
            }
            itemNorms[i] = (float) Math.sqrt(sum) + 0.000001f; // 避免除零错误
        }

        // 计算余弦相似度
        for (int i = 0; i < itemCount; i++) {
            for (int j = 0; j < itemCount; j++) {
                float dotProduct = 0;
                for (int k = 0; k < itemUserMatrix[i].length; k++) {
                    dotProduct += itemUserMatrix[i][k] * itemUserMatrix[j][k];
                }
                itemSimilarityMatrix[i][j] = dotProduct / (itemNorms[i] * itemNorms[j]);
            }
        }
    }

    /**
     * 基于用户的协同过滤推荐
     *
     * @param userId     用户ID
     * @param topN       推荐数量
     * @param kNeighbors 近邻用户数量
     * @return 推荐菜谱ID列表及其预测评分
     */
    public List<RecipeScore> userBasedRecommend(String userId, int topN, int kNeighbors) {
        if (!userIndex.containsKey(userId)) {
            return new ArrayList<>(); // 用户不存在
        }

        if (userSimilarityMatrix == null) {
            calculateUserSimilarity();
        }

        int userIdx = userIndex.get(userId);
        float[] userVector = userItemMatrix[userIdx];

        // 获取相似用户（排除自己）
        List<UserSimilarity> similarUsers = new ArrayList<>();
        for (int i = 0; i < userSimilarityMatrix.length; i++) {
            if (i != userIdx) {
                similarUsers.add(new UserSimilarity(i, userSimilarityMatrix[userIdx][i]));
            }
        }

        // 按相似度降序排序
        Collections.sort(similarUsers, new Comparator<UserSimilarity>() {
            @Override
            public int compare(UserSimilarity o1, UserSimilarity o2) {
                return Float.compare(o2.getSimilarity(), o1.getSimilarity());
            }
        });

        // 取前k个近邻
        if (similarUsers.size() > kNeighbors) {
            similarUsers = similarUsers.subList(0, kNeighbors);
        }

        // 计算预测评分
        List<RecipeScore> predictions = new ArrayList<>();
        for (int itemIdx = 0; itemIdx < itemUserMatrix.length; itemIdx++) {
            // 跳过用户已评分的物品
            if (userVector[itemIdx] > 0) {
                continue;
            }

            // 计算加权评分
            float numerator = 0;
            float denominator = 0;
            for (UserSimilarity neighbor : similarUsers) {
                int neighborIdx = neighbor.getUserIndex();
                float similarity = neighbor.getSimilarity();
                float rating = userItemMatrix[neighborIdx][itemIdx];

                if (rating > 0) { // 只考虑邻居有评分的物品
                    numerator += similarity * rating;
                    denominator += similarity;
                }
            }

            // 避免除零
            if (denominator > 0) {
                float predictedRating = numerator / denominator;
                String itemId = indexItem.get(itemIdx);
                predictions.add(new RecipeScore(itemId, predictedRating));
            }
        }

        // 按预测评分降序排序
        Collections.sort(predictions, new Comparator<RecipeScore>() {
            @Override
            public int compare(RecipeScore o1, RecipeScore o2) {
                return Float.compare(o2.getScore(), o1.getScore());
            }
        });

        // 返回top_n个推荐
        if (predictions.size() > topN) {
            return predictions.subList(0, topN);
        } else {
            return predictions;
        }
    }

    /**
     * 基于物品的协同过滤推荐
     *
     * @param userId 用户ID
     * @param topN   推荐数量
     * @return 推荐菜谱ID列表及其预测评分
     */
    public List<RecipeScore> itemBasedRecommend(String userId, int topN) {
        if (!userIndex.containsKey(userId)) {
            return new ArrayList<>(); // 用户不存在
        }

        if (itemSimilarityMatrix == null) {
            calculateItemSimilarity();
        }

        int userIdx = userIndex.get(userId);
        float[] userVector = userItemMatrix[userIdx];

        // 获取用户已评分的物品
        List<RatedItem> ratedItems = new ArrayList<>();
        for (int itemIdx = 0; itemIdx < userVector.length; itemIdx++) {
            float rating = userVector[itemIdx];
            if (rating > 0) {
                ratedItems.add(new RatedItem(itemIdx, rating));
            }
        }

        // 计算预测评分
        List<RecipeScore> predictions = new ArrayList<>();
        for (int itemIdx = 0; itemIdx < itemUserMatrix.length; itemIdx++) {
            // 跳过用户已评分的物品
            if (userVector[itemIdx] > 0) {
                continue;
            }

            // 计算加权评分
            float numerator = 0;
            float denominator = 0;
            for (RatedItem ratedItem : ratedItems) {
                int ratedItemIdx = ratedItem.getItemIndex();
                float rating = ratedItem.getRating();
                float similarity = itemSimilarityMatrix[itemIdx][ratedItemIdx];

                numerator += similarity * rating;
                denominator += Math.abs(similarity);
            }

            // 避免除零
            if (denominator > 0) {
                float predictedRating = numerator / denominator;
                String itemId = indexItem.get(itemIdx);
                predictions.add(new RecipeScore(itemId, predictedRating));
            }
        }

        // 按预测评分降序排序
        Collections.sort(predictions, new Comparator<RecipeScore>() {
            @Override
            public int compare(RecipeScore o1, RecipeScore o2) {
                return Float.compare(o2.getScore(), o1.getScore());
            }
        });

        // 返回top_n个推荐
        if (predictions.size() > topN) {
            return predictions.subList(0, topN);
        } else {
            return predictions;
        }
    }

    public float[][] getUserItemMatrix() {
        return userItemMatrix;
    }
    
    /**
     * 用户相似度内部类
     */
    private class UserSimilarity {
        private int userIndex;
        private float similarity;
        
        public UserSimilarity(int userIndex, float similarity) {
            this.userIndex = userIndex;
            this.similarity = similarity;
        }
        
        public int getUserIndex() {
            return userIndex;
        }
        
        public float getSimilarity() {
            return similarity;
        }
    }
    
    /**
     * 用户评分物品内部类
     */
    private class RatedItem {
        private int itemIndex;
        private float rating;
        
        public RatedItem(int itemIndex, float rating) {
            this.itemIndex = itemIndex;
            this.rating = rating;
        }
        
        public int getItemIndex() {
            return itemIndex;
        }
        
        public float getRating() {
            return rating;
        }
    }
}