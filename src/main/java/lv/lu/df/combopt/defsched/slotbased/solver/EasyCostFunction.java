package lv.lu.df.combopt.defsched.slotbased.solver;

import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;
import lv.lu.df.combopt.defsched.slotbased.domain.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EasyCostFunction implements EasyScoreCalculator<DefenseSchedule, SimpleScore> {
    @Override
    public SimpleScore calculateScore(DefenseSchedule defenseSchedule) {
        int cost = 0;

        for (Thesis th : defenseSchedule.getThesis()) {
            if (th.getDefenseSlot() == null) {
                cost = cost + 100; // Diplomdarbam ir jāatrod sesija, kurā to aizstāv
            } else {
                // Darba autoram ir jātiek uz aizstāvēšanos
                if (!th.getAuthor().getAvailableSlots().contains(th.getDefenseSlot())) {
                    cost = cost + 10;
                }
                // Vēlams, ka uz aizstāvēšanos tiek darba vadītājs
                if (!th.getSupervisor().getAvailableSlots().contains(th.getDefenseSlot())) {
                    cost = cost + 10;
                }
                //Vēlams, ka uz aizstāvēšanos tiek darba recenzents
                if (!th.getReviewer().getAvailableSlots().contains(th.getDefenseSlot())) {
                    cost = cost + 10;
                }
                // Darbi nav ieplānoti vienlaicīgi (vienā laika slotā)!
                if (defenseSchedule.getThesis().stream().anyMatch(th2 -> !th2.equals(th) && th2.getDefenseSlot() != null
                        && th2.getDefenseSlot().equals(th.getDefenseSlot()))) {
                    cost = cost + 50;
                }
            }
        }

        for (Person person : defenseSchedule.getPersons()) {
            List<Slot> personsSlots = defenseSchedule.getThesis().stream()
                    .filter(th -> th.getDefenseSlot() != null)
                    .filter(th -> th.getAuthor().equals(person) || th.getSupervisor().equals(person) || th.getReviewer().equals(person))
                    .map(th -> th.getDefenseSlot())
                    .collect(Collectors.toList());

            for (Slot s : personsSlots) {
                for (Slot s2 : personsSlots) {
                    // Iesaistītais netiek uz aizstāvēšanos arī tad, ja viņam ir jāveic pienākumi tanī pašā laikā citā sesijā.
                    if (s.getSlotId() < s2.getSlotId() && s.overlapsWith(s2)) cost = cost + 20;
                }
            }

            List<Session> personSessions = defenseSchedule.getSessions().stream()
                    .filter(session -> session.getSlots().stream().anyMatch(slot -> personsSlots.contains(slot)))
                    .collect(Collectors.toList());

            Integer personSessionsCount = personSessions.size();
            // Vēlams, lai iesaistītajam būtu jāapmeklē pēc iespējas mazāk sesiju.
            cost = cost + personSessionsCount * 15;

            // Vēlams, lai iesaistītajam vienā dienā nav jāapmeklē vairākas sesijas.
            for (Session s: personSessions) {
                for (Session s2: personSessions) {
                    if (s.getSessionId() > s2.getSessionId() &&
                            s.getStartingAt().toLocalDate().equals(s2.getStartingAt().toLocalDate())) {
                        cost = cost + 15;
                    }
                }
            }
        }

        for (Session session: defenseSchedule.getSessions()) {
            List<Slot> sortedSlots = session.getSlots().stream()
                    .sorted(Comparator.comparing(Slot::getStart))
                    .collect(Collectors.toList());
            int emptySlotFound = 0;

            for (Slot s: sortedSlots) {
                if (defenseSchedule.getThesis().stream().noneMatch(th -> th.getDefenseSlot() != null && th.getDefenseSlot().equals(s)))
                    emptySlotFound = emptySlotFound + 1;

                if (defenseSchedule.getThesis().stream().anyMatch(th -> th.getDefenseSlot() != null && th.getDefenseSlot().equals(s))
                        && emptySlotFound > 0)
                // Nav tukši sloti starp diplomdarbiem
                { cost = cost + emptySlotFound * 50; emptySlotFound = 0;}
            }
        }


        return SimpleScore.of(-cost);
    }
}
