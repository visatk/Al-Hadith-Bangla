# আল হাদিস বাংলা — SQLite Data Source Folder (`app/src/main/assets/databases/`)

এই ফোল্ডারটি (`app/src/main/assets/databases/`) আপনার সমস্ত **`.sqlite`** এবং **`.db`** হাদিস ডেটাবেস ফাইলের মূল Data Source হিসেবে কনফিগার করা হয়েছে।

## কিভাবে নতুন `.sqlite` ডেটাবেস যুক্ত করবেন?
1. এই ফোল্ডারে (`app/src/main/assets/databases/`) আপনার যেকোনো `.sqlite` বা `.db` ফাইল আপলোড করুন।
2. অ্যাপটি চালু হলে **`SqliteAssetManager`** স্বয়ংক্রিয়ভাবে এই ফোল্ডারের সব `.sqlite` / `.db` ফাইল স্ক্যান করবে এবং ইনডেক্স করে নেবে।
3. এছাড়া অ্যাপের ভেতরে **"ডেটাবেস ও আর্কিটেকচার (DB Studio)"** ট্যাব থেকে রানটাইমেও যেকোনো `.sqlite` ফাইল ইম্পোর্ট ও ভেরিফাই করতে পারবেন।

## সমর্থিত SQLite Table Schema (Auto-Detected)

আমাদের `SqliteSchemaAdapter` স্বয়ংক্রিয়ভাবে স্ট্যান্ডার্ড এবং জনপ্রিয় বাংলা হাদিস `.sqlite` স্কিমা (`books`, `chapters`, `sections`, `hadiths` / `hadith`) সাপোর্ট করে:

### ১. `books` টেবিল
```sql
CREATE TABLE books (
    id INTEGER PRIMARY KEY,
    title_bn TEXT NOT NULL,
    title_ar TEXT NOT NULL,
    title_en TEXT,
    author_bn TEXT,
    hadith_count INTEGER DEFAULT 0,
    chapter_count INTEGER DEFAULT 0,
    book_code TEXT,
    accent_hex TEXT,
    description_bn TEXT
);
```

### ২. `chapters` টেবিল
```sql
CREATE TABLE chapters (
    id INTEGER PRIMARY KEY,
    book_id INTEGER NOT NULL,
    chapter_number INTEGER NOT NULL,
    title_bn TEXT NOT NULL,
    title_ar TEXT,
    hadith_range TEXT,
    hadith_count INTEGER DEFAULT 0
);
```

### ৩. `sections` (পরিচ্ছেদ / বাব) টেবিল
```sql
CREATE TABLE sections (
    id INTEGER PRIMARY KEY,
    book_id INTEGER NOT NULL,
    chapter_id INTEGER NOT NULL,
    section_number TEXT,
    title_bn TEXT NOT NULL,
    preface_bn TEXT
);
```

### ৪. `hadiths` (বা `hadith`) টেবিল
```sql
CREATE TABLE hadiths (
    id INTEGER PRIMARY KEY,
    book_id INTEGER NOT NULL,
    chapter_id INTEGER NOT NULL,
    section_id INTEGER,
    hadith_number INTEGER NOT NULL,
    international_number TEXT,
    arabic_text TEXT NOT NULL,
    bangla_text TEXT NOT NULL,
    transliteration_bn TEXT,
    narrator_bn TEXT NOT NULL,
    explanation_bn TEXT,
    grade_bn TEXT NOT NULL,
    grade_type TEXT NOT NULL, -- SAHIH, HASAN, DAIF, MAWDU
    reference_bn TEXT,
    topic_tags TEXT
);
```
