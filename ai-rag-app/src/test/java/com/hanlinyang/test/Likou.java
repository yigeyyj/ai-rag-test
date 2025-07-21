package com.hanlinyang.test;

public class Likou {
    public static void main(String[] args) {
       int[] nums1 = {0};
       int[] nums2 = {1};
       new Likou().merge(nums1,0,nums2,1);
    }
    /**
     * 给你两个按 非递减顺序 排列的整数数组 nums1 和 nums2，另有两个整数 m 和 n ，分别表示 nums1 和 nums2 中的元素数目。
     * 请你 合并 nums2 到 nums1 中，使合并后的数组同样按 非递减顺序 排列。
     */
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int i = m - 1;
        int j = n - 1;
        int count = m + n - 1;
        while (i >= 0 && j >= 0) {
            if (nums1[i] >= nums2[j]) {
                nums1[count--] = nums1[i--];
            } else  {
                nums1[count--] = nums2[j--];
            }
        }
        while (j >= 0){
            nums1[count--] = nums2[j--];
        }
    }
}
