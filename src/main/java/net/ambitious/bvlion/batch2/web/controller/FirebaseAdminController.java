package net.ambitious.bvlion.batch2.web.controller;

import com.google.firebase.database.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FirebaseAdminController {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference("node/mode");

    @RequestMapping(
            value = "/firebase/rules",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public String setFirebaseAccessRules() throws InterruptedException {
        var isReadable = new Boolean[1];

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                isReadable[0] = (Boolean) snapshot.getValue();
                ref.setValueAsync(!isReadable[0]);
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });

        while (isReadable[0] == null) {
            Thread.sleep(20);
        }

        return "{\"isReadable\":" + !isReadable[0] + "}";
    }
}