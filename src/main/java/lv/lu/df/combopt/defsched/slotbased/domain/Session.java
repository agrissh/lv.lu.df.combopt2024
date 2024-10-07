package lv.lu.df.combopt.defsched.slotbased.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class Session {
    private Integer sessionId;
    private String room;
    private List<Slot> slots = new ArrayList<>();
    private LocalDateTime startingAt;
}
