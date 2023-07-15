package cm.recouvrement.banking.controller;

import cm.recouvrement.banking.entity.*;
import cm.recouvrement.banking.repository.ClientRepository;
import cm.recouvrement.banking.repository.CycleRepository;
import cm.recouvrement.banking.repository.FolderRepository;
import cm.recouvrement.banking.repository.UserRepository;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
public class FolderController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CycleRepository cycleRepository;
    @Autowired
    private ClientRepository clientRepository;

    // Ajouter un dossier
    @PostMapping("/addFolder")
    public ResponseEntity<Folder> saveFolder(@RequestBody Folder folder){

        Double pourcentage;
        Long delai = ChronoUnit.DAYS.between(LocalDate.now(), folder.getDatePromesse());
        folder.setDelai(delai);
        Client client = folder.getClient();
        if (client.getType() == Type.CLICOO){
            pourcentage = 0.2;
            folder.setPourcentage(pourcentage);
        }else if (client.getType() == Type.CLIPRI){
            pourcentage = 0.3;
            folder.setPourcentage(pourcentage);
        }else {
            pourcentage = 0.4;
            folder.setPourcentage(pourcentage);
        }
        Double montantTotal = (folder.getMontant() + (folder.getMontant()* pourcentage));
        folder.setMontantTotal(montantTotal);

        Client cl = clientRepository.findById(client.getId()).get();

        User user = folder.getUser();
        User use = userRepository.findById(user.getId()).get();

//        Cycles cycle = folder.getCycle();
//        Cycle cy = cycleRepository.findById(cycle.getId()).get();

        folder.setUser(use);
        folder.setClient(cl);
//        folder.setCycle(cycle);
        Folder saveFolder = folderRepository.save(folder);

        return new ResponseEntity<>(saveFolder, HttpStatus.OK);
    }

    //La liste de tous les dossiers
    @GetMapping("/findAllFolder")
    public List<Folder> getAllFolder(){
        return folderRepository.findAll();
    }

    // Retouner un dossier par son id: description d'un dossier
    @GetMapping("/findFolderById/{id}")
    public ResponseEntity<Folder> getFolderById(@PathVariable("id") Long id){
        Folder folder = folderRepository.findById(id).get();
        return new ResponseEntity<>(folder, HttpStatus.OK);
    }

    // La liste des dossiers actif pour un cycle donnee
//    @GetMapping("/findFolderByLevelCycle/{level}")
//    public List<Folder> getFolderByLevelCycle(@PathVariable("level") Integer level){
//
//        List<Folder> folderList = new ArrayList<>();
//        Cycle cycle = cycleRepository.findCycleByLevel(level);
//        if (cycle == null){
//            return null;
//        }
//        for (Folder folder : folderRepository.findAll()){
//            if ((folder.getCycle().getId() == cycle.getId()) && (folder.isActif()) == true){
//                folderList.add(folder);
//            }
//        }
//        return folderList;
//    }

    //Les dossiers dont la date d'expiration est atteinte
    @GetMapping("/findAllFolderExpirate")
    public List<Folder> getAllFolderExpirate(){

        List<Folder> folderList = new ArrayList<>();
        for (Folder folder : folderRepository.findAll()){
            if ((folder.expiration(folder.getDatePromesse()) == true) && (folder.isActif() == true)){
                folderList.add(folder);
            }
            Long delai = ChronoUnit.DAYS.between(LocalDate.now(), folder.getDatePromesse());
            folder.setDelai(delai);
            folderRepository.save(folder);
        }
        return folderList;
    }

    //Rechercher un dossier par son nom
    @GetMapping("/findFolderByName")
    public Optional<Folder> getFolderByName(@PathParam("nom") String nom){
        return folderRepository.findFolderByNom(nom);
    }

    //La liste des dossiers actifs
    @GetMapping("/findActifFolder")
    public List<Folder> getActifFolder(){

        List<Folder> folderList = new ArrayList<>();
        for (Folder folder : folderRepository.findAll()){
            if (folder.isActif() == true){
                folderList.add(folder);
            }
        }
        return folderList;
    }

    //Mise a jour d'un dossier, mais aussi effectuer un versement
    @PutMapping("/updateFolder/{id}")
    public ResponseEntity<Folder> updateFolder(@PathVariable("id") Long id, @RequestBody Folder folder){

        Folder existingFolder = folderRepository.findById(id).get();
        Double newVersement = existingFolder.getVersement() + folder.getVersement();
        existingFolder.setVersement(newVersement);
        if ((folder.getVersement() == existingFolder.getMontantTotal()) || (existingFolder.getMontantTotal() == 0.0)){
            boolean n = existingFolder.isActif();
            if (n == true){
                n = false;
            }
            existingFolder.setActif(n);
            Folder updateFolder = folderRepository.save(existingFolder);
            return new ResponseEntity<>(updateFolder, HttpStatus.OK);
        }
        Double montantTotal = existingFolder.getMontantTotal() - folder.getVersement();
        existingFolder.setMontantTotal(montantTotal);
        existingFolder.setCycle(folder.getCycle());
        existingFolder.setNom(folder.getNom());
        existingFolder.setUser(folder.getUser());
        existingFolder.setClient(folder.getClient());
        existingFolder.setDatePromesse(folder.getDatePromesse());
        existingFolder.setMontant(folder.getMontant());
        existingFolder.setDelai(folder.getDelai());

        Folder updateFolder = folderRepository.save(existingFolder);

        return new ResponseEntity<>(updateFolder, HttpStatus.OK);
    }

    //   region update
    /**
     *
     * @author:abdelazizabbo
     * @description:update Status and Versement
     */

    //update State Of Folder
    @PutMapping("/updateStateFolder/{id}")
    public ResponseEntity<Folder> editStateFolder(@PathVariable("id") Long id, @RequestBody Folder folder) {
        Optional<Folder> opt = folderRepository.findById(id);
        if (opt.isPresent()) {
            Folder folder1 = opt.get();
            folder1.setActif(folder.isActif());
            return new ResponseEntity<>(folderRepository.save(folder1), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //update State Of Folder
    @PutMapping("/addTransaction/{id}")
    public ResponseEntity<Folder> addNewTransaction(@PathVariable("id") Long id, @RequestBody Folder folder) {
        Optional<Folder> opt = folderRepository.findById(id);
        if (opt.isPresent()) {
            Folder folder1 = opt.get();
            folder1.setVersement(folder.getVersement());
            return new ResponseEntity<>(folderRepository.save(folder1), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    end region

}
