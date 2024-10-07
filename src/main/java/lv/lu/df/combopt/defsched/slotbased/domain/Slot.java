package lv.lu.df.combopt.defsched.slotbased.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class Slot {
    private Integer slotId;
    private LocalDateTime start;
    private LocalDateTime end;
}
