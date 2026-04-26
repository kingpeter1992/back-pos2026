package com.king.pos;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.king.pos.Dao.CaisseSessionRepository;
import com.king.pos.Dao.CategorieRepository;
import com.king.pos.Dao.ClientRepository;
import com.king.pos.Dao.DepotRepository;
import com.king.pos.Dao.FournisseurRepository;
import com.king.pos.Dao.RoleRepository;
import com.king.pos.Dao.TransactionCaisseRepository;
import com.king.pos.Dao.UserRepository;
import com.king.pos.Entitys.Categorie;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.ERole;
import com.king.pos.Entitys.Fournisseur;
import com.king.pos.Entitys.Role;
import com.king.pos.Entitys.User;

@SpringBootApplication
@EnableScheduling
public class PoseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoseApplication.class, args);
    }

    @Bean
    @Transactional
    CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            PasswordEncoder encoder,
            CaisseSessionRepository caisseSessionRepository,
            TransactionCaisseRepository transactionRepository,
                    CategorieRepository categorieRepository,
                    FournisseurRepository fournisseurRepository,
                    DepotRepository depotRepository

    ) {
        return args -> {

            // =========================
            // 0) ROLES
            // =========================
            System.out.println("🚀 Initialisation des rôles...");
            createRoleIfNotExists(roleRepository, ERole.ROLE_ADMIN);
            createRoleIfNotExists(roleRepository, ERole.ROLE_CAISSIER);
            createRoleIfNotExists(roleRepository, ERole.ROLE_RESPONSABLE_PERSONNEL);
            createRoleIfNotExists(roleRepository, ERole.ROLE_USER);

            // =========================
            // 1) ADMIN
            // =========================
            System.out.println("🚀 Initialisation utilisateur admin...");
            if (userRepository.findByEmail("kingkapeta@gmail.com").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("kingkapeta@gmail.com");
                admin.setPassword(encoder.encode("123456789"));
                admin.setActive(true);

                Set<Role> roles = new HashSet<>();
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
                roles.add(adminRole);

                admin.setRoles(roles);
                userRepository.save(admin);
                System.out.println("✅ Utilisateur ADMIN créé !");
            } else {
                System.out.println("ℹ️ Admin déjà existant.");
            }


            // =========================
        // 2) CATEGORIES PRODUITS
        // =========================
        System.out.println("🚀 Initialisation des catégories produits...");
        seedCategories(categorieRepository);
              seedFournisseur(fournisseurRepository);
              seedDepots(depotRepository);
        System.out.println("ℹ️ Seed transactions ignoré: déjà existantes.");
        System.out.println("🎉 Initialisation terminée.");
        
            // // =========================
            // // 2) SEED GARDIENS (si vide)
            // // =========================
            // if (gardienRepository.count() == 0) {
            //     seedGardiens(gardienRepository);
            // } else {
            //     System.out.println("ℹ️ Seed gardiens ignoré: déjà existants.");
            // }

            // // =========================
            // // 3) SEED CLIENTS (si vide) - ID 5 chiffres
            // // =========================
            // List<Client> clients;
            // if (clientRepository.count() == 0) {
            //     clients = seedClients(clientRepository);
            // } else {
            //     System.out.println("ℹ️ Seed clients ignoré: déjà existants.");
            //     clients = clientRepository.findAll();
            // }

            // // =========================
            // // 4) SEED CONTRATS (si vide)
            // // =========================
            // if (contratsRepository.count() == 0) {
            //     seedContrats(contratsRepository, clients);
            // } else {
            //     System.out.println("ℹ️ Seed contrats ignoré: déjà existants.");
            // }

            // // =========================
            // // 5) SEED TRANSACTIONS (si vide)
            // // =========================
            // if (transactionRepository.count() == 0) {
            //     seedTransactions(caisseSessionRepository, transactionRepository, clientRepository);
            // } else {
                System.out.println("ℹ️ Seed transactions ignoré: déjà existantes.");
            

            System.out.println("🎉 Initialisation terminée.");
        };
    }


void seedDepots(DepotRepository depotRepository) {
    System.out.println("🚀 Initialisation des dépôts...");

    if (depotRepository.count() > 0) {
        System.out.println("✔ Dépôts déjà existants, skip...");
        return;
    }

    List<Depot> depots = List.of(

            Depot.builder()
                    .nom("Dépôt Central")
                    .adresse("Centre-ville")
                    .actif(true)
                    .parDefaut(true) // dépôt principal
                    .build(),

            Depot.builder()
                    .nom("Dépôt Boutique")
                    .adresse("Commune annexe")
                    .actif(true)
                    .parDefaut(false)
                    .build(),

            Depot.builder()
                    .nom("Dépôt Secondaire")
                    .adresse("Entrepôt périphérique")
                    .actif(false)
                    .parDefaut(false)
                    .build()
    );

    depotRepository.saveAll(depots);

    System.out.println("✅ Dépôts initialisés avec succès !");
}
   
private void seedFournisseur(FournisseurRepository fournisseurRepository) {

    System.out.println("🚀 Initialisation des fournisseurs...");

    if (fournisseurRepository.count() > 0) {
        System.out.println("✔ Fournisseurs déjà existants, skip...");
        return;
    }

    List<Fournisseur> fournisseurs = List.of(

        Fournisseur.builder()
                .nom("Fournisseur Chine")
                .telephone("+86123456789")
                .email("china@supplier.com")
                .adresse("Guangzhou")
                .ville("Guangzhou")
                .pays("Chine")
                .description("Import matériel depuis Chine")
                .actif(true)
                .build(),

        Fournisseur.builder()
                .nom("Fournisseur Lubumbashi")
                .telephone("+243990000001")
                .email("lubum@supplier.com")
                .adresse("Centre ville")
                .ville("Lubumbashi")
                .pays("RDC")
                .description("Fournisseur local")
                .actif(true)
                .build(),

        Fournisseur.builder()
                .nom("Fournisseur Kinshasa")
                .telephone("+243990000002")
                .email("kin@supplier.com")
                .adresse("Gombe")
                .ville("Kinshasa")
                .pays("RDC")
                .description("Distribution nationale")
                .actif(true)
                .build()
    );

    fournisseurRepository.saveAll(fournisseurs);

    System.out.println("✅ Fournisseurs initialisés avec succès !");
}
private void seedCategories(CategorieRepository categorieRepository) {
    List<String[]> categories = List.of(
            new String[]{"Alimentation générale", "Produits alimentaires de consommation courante."},
            new String[]{"Boissons", "Eaux, sodas, jus et autres boissons."},
            new String[]{"Boissons alcoolisées", "Bières, vins, spiritueux et liqueurs."},
            new String[]{"Produits laitiers", "Lait, yaourts, fromages et dérivés."},
            new String[]{"Boulangerie", "Pain, pâtisserie et viennoiseries."},
            new String[]{"Pâtisserie", "Gâteaux, tartes et desserts sucrés."},
            new String[]{"Confiserie", "Bonbons, chocolats et friandises."},
            new String[]{"Snacks", "Chips, biscuits apéritifs et produits à grignoter."},
            new String[]{"Conserves", "Produits alimentaires en boîtes ou bocaux."},
            new String[]{"Surgelés", "Produits alimentaires congelés."},

            new String[]{"Fruits", "Fruits frais et emballés."},
            new String[]{"Légumes", "Légumes frais et conditionnés."},
            new String[]{"Épicerie", "Riz, farine, sucre, sel et produits secs."},
            new String[]{"Épices", "Épices, assaisonnements et condiments."},
            new String[]{"Huiles alimentaires", "Huiles végétales et matières grasses culinaires."},
            new String[]{"Céréales", "Maïs, blé, avoine et autres céréales."},
            new String[]{"Pâtes alimentaires", "Spaghetti, macaroni et pâtes diverses."},
            new String[]{"Riz", "Riz de différentes variétés et conditionnements."},
            new String[]{"Farines", "Farines de blé, maïs, manioc et autres."},
            new String[]{"Sucre et édulcorants", "Sucre, miel et produits sucrants."},

            new String[]{"Viandes", "Viandes fraîches ou emballées."},
            new String[]{"Poissons et fruits de mer", "Poissons frais, congelés ou conservés."},
            new String[]{"Charcuterie", "Saucisses, jambons et viandes transformées."},
            new String[]{"Volaille", "Poulet, dinde et autres volailles."},
            new String[]{"Œufs", "Œufs de consommation."},
            new String[]{"Produits bio", "Produits issus de l’agriculture biologique."},
            new String[]{"Produits diététiques", "Produits adaptés à un régime spécifique."},
            new String[]{"Produits pour bébé", "Alimentation et soins pour nourrissons."},
            new String[]{"Café", "Café moulu, en grains ou soluble."},
            new String[]{"Thé et infusions", "Thés, tisanes et infusions."},

            new String[]{"Hygiène corporelle", "Produits de toilette et de soin du corps."},
            new String[]{"Cosmétiques", "Maquillage, parfums et produits de beauté."},
            new String[]{"Soins capillaires", "Shampoings, gels et traitements pour cheveux."},
            new String[]{"Savons et gels douche", "Produits lavants pour le corps."},
            new String[]{"Parfums", "Eaux de parfum, eaux de toilette et sprays."},
            new String[]{"Produits dentaires", "Brosses à dents, dentifrices et bains de bouche."},
            new String[]{"Hygiène féminine", "Serviettes, tampons et protections féminines."},
            new String[]{"Couches et lingettes", "Couches bébé et lingettes nettoyantes."},
            new String[]{"Entretien ménager", "Produits de nettoyage pour maison et surfaces."},
            new String[]{"Lessives", "Détergents pour lavage des vêtements."},

            new String[]{"Désinfectants", "Produits pour désinfection et assainissement."},
            new String[]{"Papeterie", "Cahiers, stylos, feuilles et accessoires de bureau."},
            new String[]{"Fournitures scolaires", "Articles pour école et études."},
            new String[]{"Fournitures de bureau", "Classeurs, agrafeuses, enveloppes et accessoires."},
            new String[]{"Livres", "Livres scolaires, romans et documents imprimés."},
            new String[]{"Jouets", "Jouets pour enfants et articles ludiques."},
            new String[]{"Jeux vidéo", "Consoles, jeux et accessoires gaming."},
            new String[]{"Sport et loisirs", "Articles de sport, fitness et détente."},
            new String[]{"Vêtements hommes", "Habits et accessoires pour hommes."},
            new String[]{"Vêtements femmes", "Habits et accessoires pour femmes."},

            new String[]{"Vêtements enfants", "Habits et accessoires pour enfants."},
            new String[]{"Chaussures", "Chaussures pour tous usages."},
            new String[]{"Accessoires de mode", "Sacs, ceintures, lunettes et bijoux."},
            new String[]{"Bijoux", "Bijoux fantaisie ou précieux."},
            new String[]{"Montres", "Montres et bracelets connectés."},
            new String[]{"Bagagerie", "Valises, sacs de voyage et cartables."},
            new String[]{"Téléphones", "Smartphones, téléphones classiques et mobiles."},
            new String[]{"Accessoires téléphoniques", "Chargeurs, écouteurs, coques et câbles."},
            new String[]{"Informatique", "Ordinateurs, périphériques et composants."},
            new String[]{"Accessoires informatiques", "Claviers, souris, clés USB et autres."},

            new String[]{"Électronique", "Appareils électroniques grand public."},
            new String[]{"Électroménager", "Appareils pour la maison et la cuisine."},
            new String[]{"Téléviseurs et audio", "TV, radios, enceintes et home cinéma."},
            new String[]{"Caméras et sécurité", "Caméras, alarmes et dispositifs de sécurité."},
            new String[]{"Éclairage", "Ampoules, lampes et accessoires lumineux."},
            new String[]{"Électricité", "Câbles, prises, disjoncteurs et équipements électriques."},
            new String[]{"Outillage", "Outils manuels et électriques."},
            new String[]{"Quincaillerie", "Vis, clous, serrures et articles de bricolage."},
            new String[]{"Matériaux de construction", "Ciment, sable, peinture et matériaux divers."},
            new String[]{"Peinture et accessoires", "Peintures, pinceaux, rouleaux et solvants."},

            new String[]{"Plomberie", "Robinets, tuyaux et accessoires sanitaires."},
            new String[]{"Sanitaires", "WC, lavabos, douches et équipements sanitaires."},
            new String[]{"Menuiserie", "Bois, portes, fenêtres et éléments de menuiserie."},
            new String[]{"Mobilier", "Meubles de maison et de bureau."},
            new String[]{"Décoration", "Objets décoratifs et accessoires d’intérieur."},
            new String[]{"Linge de maison", "Draps, rideaux, serviettes et couvertures."},
            new String[]{"Cuisine et vaisselle", "Ustensiles, casseroles et vaisselle."},
            new String[]{"Articles ménagers", "Produits utiles pour la maison."},
            new String[]{"Jardinage", "Outils et produits pour jardin et espaces verts."},
            new String[]{"Animaux", "Produits et accessoires pour animaux domestiques."},

            new String[]{"Santé et pharmacie", "Produits de santé, parapharmacie et bien-être."},
            new String[]{"Compléments alimentaires", "Vitamines, minéraux et suppléments."},
            new String[]{"Matériel médical", "Thermomètres, tensiomètres et équipements médicaux."},
            new String[]{"Automobile", "Produits et accessoires pour véhicules."},
            new String[]{"Pièces automobiles", "Pièces de rechange auto."},
            new String[]{"Lubrifiants et huiles moteur", "Huiles, graisses et fluides techniques."},
            new String[]{"Pneumatiques", "Pneus et chambres à air."},
            new String[]{"Batteries", "Batteries pour véhicules et appareils."},
            new String[]{"Motos et accessoires", "Articles et pièces pour motos."},
            new String[]{"Vélos et accessoires", "Vélos, pièces et équipements associés."},

            new String[]{"Matériel industriel", "Équipements et fournitures industriels."},
            new String[]{"Sécurité industrielle", "Casques, gants, EPI et équipements de protection."},
            new String[]{"Équipements professionnels", "Matériels destinés aux professionnels."},
            new String[]{"Impression et reprographie", "Imprimantes, toners et consommables."},
            new String[]{"Accessoires photo", "Appareils photo et accessoires."},
            new String[]{"Musique et instruments", "Instruments de musique et accessoires."},
            new String[]{"Articles religieux", "Bibles, chapelets et objets religieux."},
            new String[]{"Souvenirs et cadeaux", "Cadeaux, gadgets et objets souvenir."},
            new String[]{"Produits locaux", "Articles issus de la production locale."},
            new String[]{"Importation", "Produits importés de l’étranger."},

            new String[]{"Produits de luxe", "Articles haut de gamme et premium."},
            new String[]{"Produits promotionnels", "Articles en promotion ou en offre spéciale."},
            new String[]{"Déstockage", "Produits en liquidation ou fin de série."},
            new String[]{"Produits saisonniers", "Articles vendus selon la saison."},
            new String[]{"Produits en rupture", "Catégorie de suivi des produits indisponibles."},
            new String[]{"Produits à forte rotation", "Produits qui se vendent rapidement."},
            new String[]{"Produits à faible rotation", "Produits qui se vendent lentement."},
            new String[]{"Nouveautés", "Nouveaux produits récemment ajoutés."},
            new String[]{"Services", "Prestations ou services facturables."},
            new String[]{"Autres", "Catégorie générique pour produits divers."}
    );

    int inserted = 0;

    for (String[] data : categories) {
        String nom = data[0];
        String description = data[1];

        if (!categorieRepository.existsByNomIgnoreCase(nom)) {
            Categorie categorie = new Categorie();
            categorie.setNom(nom);
            categorie.setDescription(description);
            categorie.setActif(true);
            categorie.setDateCreation(LocalDateTime.now());
            categorieRepository.save(categorie);
            inserted++;
            System.out.println("✔ Catégorie créée : " + nom);
        }
    }

    System.out.println("✅ Catégories insérées : " + inserted);
}

    private void createRoleIfNotExists(RoleRepository repository, ERole roleName) {
        if (repository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            repository.save(role);
            System.out.println("✔ Role créé : " + roleName);
        }
    }
}