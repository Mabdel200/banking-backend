package cm.recouvrement.banking.controller;

import cm.recouvrement.banking.entity.Folder;
import cm.recouvrement.banking.entity.Role;
import cm.recouvrement.banking.entity.User;
import cm.recouvrement.banking.repository.FolderRepository;
import cm.recouvrement.banking.repository.UserRepository;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FolderRepository folderRepository;

//    @PostMapping(value = "/addUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<User> saveUser(@RequestParam("file") MultipartFile file, User user) throws IOException {
//
//        String dir = System.getProperty("user.dir");
//        String url = dir+"/src/main/resources/assets/"+file.getOriginalFilename();
//        user.setPhoto(url);
//        User saveUser = userRepository.save(user);
//        File convertFile = new File(url);
//        convertFile.createNewFile();
//        try(FileOutputStream out = new FileOutputStream(convertFile)){
//            out.write(file.getBytes());
//        }catch (Exception exe){
//            exe.printStackTrace();
//        }
//        return new ResponseEntity<>(saveUser, HttpStatus.OK);
//    }

    @PostMapping("/addUser")
    public ResponseEntity<User> saveUser(@RequestBody User user)  {
        User saveUser = userRepository.save(user);
        return new ResponseEntity<>(saveUser, HttpStatus.OK);
    }

    //La liste de tous les users
    @GetMapping("/findAllUser")
    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    //La liste de tous les users par type de user: agent ou admin
    @GetMapping("/findAllUserByRole/{role}")
    public List<User> getAllUserAgent(@PathVariable("role") Role role){

        List<User> userList = new ArrayList<>();
        for (User user : userRepository.findAll()){
            if (user.getRole() == role){
                userList.add(user);
            }
        }

        return userList;
    }
    //Rechercher un dossier par son nom
//    @GetMapping("/findUserByRole")
//    public Optional<User> getUserByName(@PathParam("nom") String nom){
//        return folderRepository.findFolderByNom(nom);
//    }

    @GetMapping("/findUserById/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id){
        User user = userRepository.findById(id).get();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    //Liste des dossiers d'un agent: le porte feuille agent
    @GetMapping("/findAllFolderFromUserAgent/{id}")
    public List<Folder> getAllFolderFromUser(@PathVariable("id") Long id){

        User user = userRepository.findById(id).get();
        List<Folder> folderList = new ArrayList<>();
        for(Folder folder : folderRepository.findAll()){
            if ((folder.getUser().getId() == user.getId()) && (user.getRole() == Role.Agent)){
                folderList.add(folder);
            }
        }
        return folderList;
    }

    //Liste des dossiers actifs d'un agent: le porte feuille agent
    @GetMapping("/findActifFolderFromUserAgent/{id}")
    public List<Folder> getActifFolderFromUser(@PathVariable("id") Long id){

        User user = userRepository.findById(id).get();
        List<Folder> folderList = new ArrayList<>();
        for(Folder folder : folderRepository.findAll()){
            if ((folder.getUser().getId() == user.getId()) && (user.getRole() == Role.Agent) && (folder.isActif() == true)){
                folderList.add(folder);
            }
        }
        return folderList;
    }
//   region update
    /**
     *
     * @author:abdelazizabbo
     * @description:update user and state
     */
    //update Year
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<User> editUser(@PathVariable("id") Long id, @RequestBody User user) {

        Optional<User> opt = userRepository.findById(id);

        if (opt.isPresent()) {
            User user1 = opt.get();
            user1.setUsername(user.getUsername());
            user1.setEmail(user.getEmail());
            user1.setLogin(user.getLogin());
            user1.setTelephone(user.getTelephone());
            user1.setRole(user.getRole());

            return new ResponseEntity<>(userRepository.save(user1), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    //update State Of Year
    @PutMapping("/updateStateUser/{id}")
    public ResponseEntity<User> editStateUser(@PathVariable("id") Long id, @RequestBody User user) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isPresent()) {
            User user1 = opt.get();
            user1.setActif(user.isActif());
            return new ResponseEntity<>(userRepository.save(user1), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    end region
    

}
