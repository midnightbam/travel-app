package com.techup.travel_app;

import com.techup.travel_app.entity.Trip;
import com.techup.travel_app.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private TripRepository tripRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Skip initialization - using existing Supabase data
        System.out.println("Skipping data initialization - using existing Supabase data");
    }
    
    /*
    private void initializeData() {
        Trip trip1 = new Trip();
        trip1.setTitle("คู่มือเที่ยวเกาะช้าง กิน เที่ยว พักที่ไหนดี? อ่านจบครบที่เดียว!");
        trip1.setDescription("วันว่างนี้ไปเที่ยวเกาะช้างกัน พร้อมทำกิจกรรมต่าง ๆ เช่น เที่ยวน้ำตก ล่องเรือชมป่าชายเลน ขี่ช้างท่องป่า ผจญภัยเหนือยอดไม้ และดำน้ำตื้น รับรองทริปนี้สนุกแน่!");
        trip1.setLocation("เกาะช้าง, ตราด");
        trip1.setDuration("3 วัน 2 คืน");
        trip1.setRating(4.8);
        trip1.setPrice(3500.00);
        trip1.setPhotos(new String[]{
            "https://img.wongnai.com/p/1600x0/2019/07/02/3c758646aa6c426ba3c6a81f57b20bd6.jpg",
            "https://img.wongnai.com/p/1600x0/2019/07/02/6a2733ab91164ac8943b77deb14fdbde.jpg",
            "https://img.wongnai.com/p/1600x0/2019/07/02/941dbb607f0742bbadd63bbbd993e187.jpg"
        });
        trip1.setTags(new String[]{"เกาะ", "ทะเล", "จุดชมวิว", "ธรรมชาติ", "ตราด"});
        tripRepository.save(trip1);
        
        Trip trip2 = new Trip();
        trip2.setTitle("ลัดเลาะ 10 ที่เที่ยวย่าน BTS สายสีเขียว");
        trip2.setDescription("BTS สายสีเขียวเปิดให้บริการเพิ่ม 5 สถานีทั้งที งานนี้ต้องไม่มีพลาด รีบไปอัปเดต เช็กอินที่เที่ยวคูล ๆ ไปได้ง่ายก่อนใคร");
        trip2.setLocation("กรุงเทพฯ");
        trip2.setDuration("1 วัน");
        trip2.setRating(4.3);
        trip2.setPrice(1500.00);
        trip2.setPhotos(new String[]{
            "https://img.wongnai.com/p/1600x0/2020/02/18/458b9a31b62b408d91137fbe152f7450.jpg",
            "https://img.wongnai.com/p/1600x0/2020/02/18/08bca1c784db4d7f92e93656b5376681.jpg"
        });
        trip2.setTags(new String[]{"คาเฟ่", "ร้านกาแฟ", "จุดถ่ายรูป", "เที่ยวใกล้กรุง", "กรุงเทพมหานคร"});
        tripRepository.save(trip2);
        
        Trip trip3 = new Trip();
        trip3.setTitle("เที่ยวทุ่งทานตะวันชลบุรี ได้รูปสวยไม่ต้องไปไกลที่ ไร่คีรีบูรพา");
        trip3.setDescription("บานสะพรั่งแล้วจ้า กับทุ่งทานตะวันที่ใหญ่กว่า 30 ไร่ แต่อย่าชะล่าใจไป เพราะว่าเปิดให้เข้าชมถึงแค่เดือนมกราคมเท่านั้น");
        trip3.setLocation("ชลบุรี");
        trip3.setDuration("1 วัน");
        trip3.setRating(4.7);
        trip3.setPrice(800.00);
        trip3.setRating(4.7);
        trip3.setPrice(4200.00);
        trip3.setPhotos(new String[]{
            "https://img.wongnai.com/p/1600x0/2019/12/27/777681c6184b498baead821895f7d7ab.jpg",
            "https://img.wongnai.com/p/1600x0/2019/12/27/79e5b71efa54433b909e08bac13f27bb.jpg"
        });
        trip3.setTags(new String[]{"จุดถ่ายรูป", "ถ่ายรูปสวย", "เที่ยวใกล้กรุง", "ชลบุรี"});
        tripRepository.save(trip3);
        
        Trip trip4 = new Trip();
        trip4.setTitle("สมุทรปราการ การผจญภัยใหม่ใกล้กรุงเทพ");
        trip4.setDescription("สถานที่ท่องเที่ยวยอดนิยมใกล้กรุงเทพฯ ที่มีความเป็นเอกลักษณ์ทั้งธรรมชาติและวัฒนธรรม");
        trip4.setLocation("สมุทรปราการ");
        trip4.setDuration("1 วัน");
        trip4.setRating(4.1);
        trip4.setPrice(1500.00);
        trip4.setPhotos(new String[]{
            "https://img.wongnai.com/p/1600x0/2019/12/25/54961e4326954765a80cd20e2044083d.jpg",
            "https://img.wongnai.com/p/1600x0/2019/12/25/183af5673b074e55a3842aca97676220.jpg"
        });
        trip4.setTags(new String[]{"จุดถ่ายรูป", "หมู่บ้าน", "ภูเขา", "ธรรมชาติ", "กาญจนบุรี"});
        tripRepository.save(trip4);
        
        Trip trip5 = new Trip();
        trip5.setTitle("เขาใหญ่ สนุกสนานกับธรรมชาติอันบริสุทธิ์");
        trip5.setDescription("อุทยานแห่งชาติเขาใหญ่ ดินแดนแห่งความหลากหลายทางชีวภาพที่สมบูรณ์และธรรมชาติที่งดงาม");
        trip5.setLocation("เขาใหญ่");
        trip5.setDuration("2 วัน 1 คืน");
        trip5.setRating(4.6);
        trip5.setPrice(2200.00);
        trip5.setPhotos(new String[]{
            "https://img.wongnai.com/p/1600x0/2019/10/10/3092e773cda34ca2a77373b82f7639b7.jpg",
            "https://img.wongnai.com/p/1600x0/2019/10/10/7739627a33134587827ee72f1a9c2cce.jpg"
        });
        trip5.setTags(new String[]{"จุดถ่ายรูป", "ถ่ายรูปสวย", "ร้านกาแฟ", "เชียงใหม่", "คาเฟ่"});
        tripRepository.save(trip5);
        
        Trip trip6 = new Trip();
        trip6.setTitle("สุรินทร์ ดินแดนแห่งมหัศจรรย์ของช้าง");
        trip6.setDescription("สัมผัสเสน่ห์ของแผ่นดินอีสาน ดินแดนแห่งผ้าไหมและประวัติศาสตร์ที่ยาวนาน");
        trip6.setLocation("สุรินทร์");
        trip6.setDuration("3 วัน 2 คืน");
        trip6.setRating(4.4);
        trip6.setPrice(2900.00);
        trip6.setPhotos(new String[]{
            "https://img.wongnai.com/p/1600x0/2019/07/31/b3986319d85a4d85baecbacf03a519d8.jpg",
            "https://img.wongnai.com/p/1600x0/2019/07/31/9969a296fe1b452a8e64c2b7cfd58319.jpg"
        });
        trip6.setTags(new String[]{"ทะเล", "เกาะ", "สตูล", "ธรรมชาติ"});
        tripRepository.save(trip6);
        
        System.out.println("Sample trips data initialized successfully!");
    }
    */
}