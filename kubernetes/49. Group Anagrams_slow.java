class Solution {
    public List<List<String>> groupAnagrams(String[] strs) {
        // 각 문자의 빈도수 체크 hashmap
        HashMap<HashMap<Character, Integer>, List<String>> hm = new HashMap<>();
        
        for(String str : strs) {
            // 아나그램 생성 및 저장
            HashMap<Character, Integer> anaMap = new HashMap<>();
            for(int i=0; i<str.length(); i++) {
                anaMap.put(str.charAt(i), anaMap.getOrDefault(str.charAt(i), 0) + 1);
            }
            List<String> adder = hm.getOrDefault(anaMap, new ArrayList<>());
            adder.add(str);
            hm.put(anaMap, adder);
        }

        List<List<String>> ans = new ArrayList<>();

        // 확인
        for(Map.Entry<HashMap<Character,Integer>,List<String>> entry : hm.entrySet()) {
            ans.add(entry.getValue());
        }

        return ans;
    }
}

