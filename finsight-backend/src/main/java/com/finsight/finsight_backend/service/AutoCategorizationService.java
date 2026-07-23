package com.finsight.finsight_backend.service;

import com.finsight.finsight_backend.entity.Category;
import com.finsight.finsight_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutoCategorizationService {

    private final CategoryRepository categoryRepository;

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
        "Food", List.of(
            "swiggy", "zomato", "dominos", "pizza", "mcdonalds", "kfc",
            "burger", "restaurant", "cafe", "food", "eat", "dining",
            "starbucks", "subway", "dunkin", "bakery", "hotel"
        ),
        "Travel", List.of(
            "uber", "ola", "rapido", "redbus", "irctc", "makemytrip",
            "goibibo", "airline", "flight", "train", "bus", "metro",
            "cab", "taxi", "petrol", "fuel", "toll", "parking"
        ),
        "Shopping", List.of(
            "amazon", "flipkart", "myntra", "ajio", "nykaa", "meesho",
            "snapdeal", "shopping", "mall", "store", "market", "retail",
            "clothes", "fashion", "shoes", "watch"
        ),
        "Bills", List.of(
            "electricity", "water", "gas", "internet", "broadband",
            "airtel", "jio", "bsnl", "vi", "vodafone", "bill",
            "recharge", "utility", "maintenance", "rent"
        ),
        "Entertainment", List.of(
            "netflix", "amazon prime", "hotstar", "disney", "spotify",
            "youtube", "movie", "cinema", "pvr", "inox", "bookmyshow",
            "gaming", "steam", "concert", "show", "ticket"
        ),
        "Healthcare", List.of(
            "apollo", "medplus", "pharmacy", "hospital", "clinic",
            "doctor", "medicine", "health", "dental", "lab", "test",
            "diagnostic", "care", "medical", "drug"
        ),
        "Education", List.of(
            "udemy", "coursera", "college", "university", "school",
            "tuition", "coaching", "books", "stationery", "course",
            "learning", "institute", "fees", "exam"
        )
    );

    public Category categorize(String title, String merchantName) {
        String searchText = "";
        if (merchantName != null && !merchantName.isEmpty()) {
            searchText = merchantName.toLowerCase();
        } else if (title != null && !title.isEmpty()) {
            searchText = title.toLowerCase();
        }

        if (searchText.isEmpty()) {
            return getCategory("Other");
        }

        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (searchText.contains(keyword)) {
                    return getCategory(entry.getKey());
                }
            }
        }

        return getCategory("Other");
    }

    private Category getCategory(String name) {
        return categoryRepository.findByName(name).orElse(null);
    }
}
