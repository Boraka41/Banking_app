Banking App

Açıklama:
Bu proje, Spring Boot framework'ü kullanarak geliştirilmiş kapsamlı bir bankacılık uygulamasıdır. Kullanıcıların hesap yönetimi, para transferleri ve işlem geçmişi gibi işlemleri gerçekleştirebileceği modern bir platform sunar. Uygulama, güvenlik, cache yönetimi ve veri akışı için Keycloak, Redis ve Kafka kullanmaktadır.

🔧 Teknolojiler

Backend: Java, Spring Boot

Veritabanı: MySQL

Bağımlılıklar: Spring Security, JWT, Spring Data JPA, Spring Kafka, Redis

Yetkilendirme: Keycloak

Mesajlaşma: Kafka

Cache Yönetimi: Redis

🚀 Başlarken
1. Projeyi Klonlayın
git clone https://github.com/Boraka41/Banking_app.git
cd Banking_app

2. Bağımlılıkları Yükleyin

Projenizde Maven kullanılıyorsa, aşağıdaki komutu çalıştırabilirsiniz:

./mvnw install

3. Uygulamayı Çalıştırın
./mvnw spring-boot:run


Uygulama varsayılan olarak http://localhost:8080 adresinde çalışacaktır.

🧪 Özellikler
Keycloak ile Yetkilendirme

Keycloak kullanılarak merkezi bir kimlik doğrulama yönetimi sağlanmaktadır. Kullanıcılar, Keycloak üzerinden kayıt olabilir, giriş yapabilir ve rol tabanlı erişim kontrolü ile korunmaktadır.

Proje, Keycloak ile entegrasyon sağlayarak güvenli bir kullanıcı yönetimi sunmaktadır. Keycloak'tan alınan JWT token ile API'lere erişim sağlanır.

Keycloak Kurulumu:

Keycloak, Docker üzerinden çalıştırılabilir. Örnek bir Docker komutu ile Keycloak'ı başlatabilirsiniz:

docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin jboss/keycloak


Keycloak'ı başlattıktan sonra, admin paneline http://localhost:8080 üzerinden erişebilir ve kullanıcıları, grupları ve rollerini yönetebilirsiniz.

Redis ile Cache Yönetimi

Redis, uygulamanın hızlı veri erişimini sağlayan bir cache mekanizması olarak kullanılır. Kullanıcı giriş bilgileri, işlem geçmişi gibi sık erişilen veriler Redis üzerinde saklanarak veritabanı yükü azaltılır.

Cache Yapılandırması:
Uygulama, Redis ile entegre çalışır. Redis'in Docker üzerinden çalıştırılması gereklidir:

docker run --name redis -p 6379:6379 -d redis


Cache Kullanımı:
Redis, Spring Cache Manager ile entegre edilmiştir. Örnek olarak, kullanıcı verilerini cache'e almak için şu şekilde bir yapı kullanılabilir:

@Cacheable(value = "users", key = "#id")
public User findUserById(Long id) {
    return userRepository.findById(id).orElse(null);
}

Kafka ile Mesajlaşma ve Veri Akışı

Apache Kafka, uygulama içinde asenkron mesajlaşma ve veri akışı sağlamak için kullanılır. Kafka, özellikle yüksek hacimli veri işleme ve mikro servisler arası iletişim için etkilidir.

Kafka Kurulumu:
Kafka, Docker üzerinden çalıştırılabilir. Kafka'yı başlatmak için şu komutları kullanabilirsiniz:

docker-compose -f https://raw.githubusercontent.com/wurstmeister/kafka-docker/master/docker-compose.yml up


Kafka Entegrasyonu:
Spring Kafka kullanılarak, mesaj üreticileri (producers) ve tüketiciler (consumers) ile veri akışı sağlanır. Örneğin, bir kredi kartı başvurusu yapılırken, başvuru sahibinin kredi limitine bakılır. 

Eğer kredi limiti yeterliyse, Kafka üzerinden bir mesaj gönderilir ve başka bir mikroservis bu mesajı alarak kredi kartı oluşturma işlemini başlatır. 

🤝 Katkıda Bulunanlar
Bu proje aşağıdaki geliştirici tarafından hazırlanmıştır:
Bora Kırarslan
