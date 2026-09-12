package com.sahayatri.sahayatribackend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*")
public class BudgetGroupController {

    private final TripGroupRepository tripGroupRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripExpenseRepository tripExpenseRepository;
    private final TripRepository tripRepository;

    public BudgetGroupController(
            TripGroupRepository tripGroupRepository,
            TripMemberRepository tripMemberRepository,
            TripExpenseRepository tripExpenseRepository,
            TripRepository tripRepository) {

        this.tripGroupRepository = tripGroupRepository;
        this.tripMemberRepository = tripMemberRepository;
        this.tripExpenseRepository = tripExpenseRepository;
        this.tripRepository = tripRepository;
    }

    // ==========================================
    // CREATE GROUP FOR A SAVED TRIP
    // ==========================================

    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(
            @RequestBody Map<String, Object> request) {

        try {

            Long tripId = Long.valueOf(
                    String.valueOf(request.get("tripId"))
            );

            String email = String.valueOf(
                    request.get("createdByEmail")
            );

            String groupName = String.valueOf(
                    request.get("groupName")
            );

            String creatorName = String.valueOf(
                    request.get("creatorName")
            );

            String creatorPhone = String.valueOf(
                    request.getOrDefault("creatorPhone", "")
            );

            String creatorUpiId = String.valueOf(
                    request.getOrDefault("creatorUpiId", "")
            );

            // Check trip
            Optional<Trip> tripOptional =
                    tripRepository.findById(tripId);

            if (tripOptional.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message",
                                "Trip not found."
                        ));
            }

            Trip trip = tripOptional.get();

            // Check owner
            if (trip.getEmail() == null ||
                    !trip.getEmail().equalsIgnoreCase(email)) {

                return ResponseEntity
                        .status(403)
                        .body(Map.of(
                                "message",
                                "You are not allowed to create a group for this trip."
                        ));
            }

            // Check if group already exists
            Optional<TripGroup> existingGroup =
                    tripGroupRepository.findByTripId(tripId);

            if (existingGroup.isPresent()) {

                return ResponseEntity.ok(
                        buildGroupResponse(
                                existingGroup.get()
                        )
                );
            }

            if (groupName == null ||
                    groupName.equals("null") ||
                    groupName.trim().isEmpty()) {

                groupName = trip.getLocation() + " Trip";
            }

            // Create group
            TripGroup group = new TripGroup(
                    tripId,
                    groupName,
                    email
            );

            TripGroup savedGroup =
                    tripGroupRepository.save(group);

            // Add creator as first member
            if (creatorName == null ||
                    creatorName.equals("null") ||
                    creatorName.trim().isEmpty()) {

                creatorName = "You";
            }

            TripMember creator = new TripMember(
                    savedGroup.getId(),
                    creatorName,
                    email,
                    creatorPhone,
                    creatorUpiId
            );

            tripMemberRepository.save(creator);

            return ResponseEntity.ok(
                    buildGroupResponse(savedGroup)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Unable to create group.",
                            "error",
                            e.getMessage()
                    ));
        }
    }


    // ==========================================
    // GET GROUP BY TRIP ID
    // ==========================================

    @GetMapping("/groups/trip/{tripId}")
    public ResponseEntity<?> getGroupByTrip(
            @PathVariable Long tripId) {

        Optional<TripGroup> group =
                tripGroupRepository.findByTripId(tripId);

        if (group.isEmpty()) {

            return ResponseEntity
                    .status(404)
                    .body(Map.of(
                            "message",
                            "No group created for this trip."
                    ));
        }

        return ResponseEntity.ok(
                buildGroupResponse(group.get())
        );
    }


    // ==========================================
    // ADD MEMBER
    // ==========================================

    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<?> addMember(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {

        if (!tripGroupRepository.existsById(groupId)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        String name = String.valueOf(
                request.get("name")
        );

        String email = String.valueOf(
                request.getOrDefault("email", "")
        );

        String phone = String.valueOf(
                request.getOrDefault("phone", "")
        );

        String upiId = String.valueOf(
                request.getOrDefault("upiId", "")
        );

        if (name.equals("null") ||
                name.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Friend name is required."
                    ));
        }

        TripMember member = new TripMember(
                groupId,
                name.trim(),
                email,
                phone,
                upiId
        );

        TripMember savedMember =
                tripMemberRepository.save(member);

        return ResponseEntity.ok(savedMember);
    }


    // ==========================================
    // GET MEMBERS
    // ==========================================

    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<List<TripMember>> getMembers(
            @PathVariable Long groupId) {

        return ResponseEntity.ok(
                tripMemberRepository
                        .findByGroupIdOrderByIdAsc(groupId)
        );
    }


    // ==========================================
    // DELETE MEMBER
    // ==========================================

    @DeleteMapping("/groups/{groupId}/members/{memberId}")
    public ResponseEntity<?> deleteMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {

        Optional<TripMember> member =
                tripMemberRepository.findById(memberId);

        if (member.isEmpty() ||
                !member.get().getGroupId().equals(groupId)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        // Don't allow deleting a member
        // who has already paid an expense
        if (tripExpenseRepository
                .existsByPaidByMemberId(memberId)) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "This member has expenses. Delete those expenses first."
                    ));
        }

        tripMemberRepository.deleteById(memberId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Member removed successfully."
                )
        );
    }


    // ==========================================
    // ADD EXPENSE
    // ==========================================

    @PostMapping("/groups/{groupId}/expenses")
    public ResponseEntity<?> addExpense(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {

        if (!tripGroupRepository.existsById(groupId)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        String description = String.valueOf(
                request.get("description")
        );

        double amount = Double.parseDouble(
                String.valueOf(
                        request.get("amount")
                )
        );

        Long paidByMemberId = Long.valueOf(
                String.valueOf(
                        request.get("paidByMemberId")
                )
        );

        if (description.equals("null") ||
                description.trim().isEmpty() ||
                amount <= 0) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Enter a valid expense and amount."
                    ));
        }

        Optional<TripMember> member =
                tripMemberRepository.findById(
                        paidByMemberId
                );

        if (member.isEmpty() ||
                !member.get().getGroupId().equals(groupId)) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Invalid member."
                    ));
        }

        TripExpense expense = new TripExpense(
                groupId,
                description.trim(),
                amount,
                paidByMemberId
        );

        TripExpense savedExpense =
                tripExpenseRepository.save(expense);

        return ResponseEntity.ok(savedExpense);
    }


    // ==========================================
    // GET EXPENSES
    // ==========================================

    @GetMapping("/groups/{groupId}/expenses")
    public ResponseEntity<List<TripExpense>> getExpenses(
            @PathVariable Long groupId) {

        return ResponseEntity.ok(
                tripExpenseRepository
                        .findByGroupIdOrderByCreatedAtAsc(groupId)
        );
    }


    // ==========================================
    // DELETE EXPENSE
    // ==========================================

    @DeleteMapping("/groups/{groupId}/expenses/{expenseId}")
    public ResponseEntity<?> deleteExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId) {

        Optional<TripExpense> expense =
                tripExpenseRepository.findById(expenseId);

        if (expense.isEmpty() ||
                !expense.get().getGroupId().equals(groupId)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        tripExpenseRepository.deleteById(expenseId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Expense deleted successfully."
                )
        );
    }


    // ==========================================
    // GROUP SUMMARY
    // ==========================================

    @GetMapping("/groups/{groupId}/summary")
    public ResponseEntity<?> getSummary(
            @PathVariable Long groupId) {

        Optional<TripGroup> group =
                tripGroupRepository.findById(groupId);

        if (group.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                calculateSummary(group.get())
        );
    }


    // ==========================================
    // COMPLETE GROUP RESPONSE
    // ==========================================

    private Map<String, Object> buildGroupResponse(
            TripGroup group) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("group", group);

        Optional<Trip> trip =
                tripRepository.findById(
                        group.getTripId()
                );

        trip.ifPresent(value ->
                response.put("trip", value)
        );

        response.put(
                "members",
                tripMemberRepository
                        .findByGroupIdOrderByIdAsc(
                                group.getId()
                        )
        );

        response.put(
                "expenses",
                tripExpenseRepository
                        .findByGroupIdOrderByCreatedAtAsc(
                                group.getId()
                        )
        );

        response.put(
                "summary",
                calculateSummary(group)
        );

        return response;
    }


    // ==========================================
    // CALCULATE SPLIT
    // ==========================================

    private Map<String, Object> calculateSummary(
            TripGroup group) {

        List<TripMember> members =
                tripMemberRepository
                        .findByGroupIdOrderByIdAsc(
                                group.getId()
                        );

        List<TripExpense> expenses =
                tripExpenseRepository
                        .findByGroupIdOrderByCreatedAtAsc(
                                group.getId()
                        );

        double total = 0;

        Map<Long, Double> paid =
                new LinkedHashMap<>();

        for (TripMember member : members) {
            paid.put(member.getId(), 0.0);
        }

        for (TripExpense expense : expenses) {

            total += expense.getAmount();

            paid.put(
                    expense.getPaidByMemberId(),
                    paid.getOrDefault(
                            expense.getPaidByMemberId(),
                            0.0
                    ) + expense.getAmount()
            );
        }

        double share = members.isEmpty()
                ? 0
                : total / members.size();

        List<Map<String, Object>> memberSummary =
                new ArrayList<>();

        for (TripMember member : members) {

            double paidAmount =
                    paid.getOrDefault(
                            member.getId(),
                            0.0
                    );

            double balance =
                    paidAmount - share;

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put("memberId", member.getId());
            item.put("name", member.getName());
            item.put("upiId", member.getUpiId());
            item.put("paid", round(paidAmount));
            item.put("share", round(share));
            item.put("balance", round(balance));

            memberSummary.add(item);
        }

        // ============================
        // SETTLEMENT
        // ============================

        List<Map<String, Object>> debtors =
                new ArrayList<>();

        List<Map<String, Object>> creditors =
                new ArrayList<>();

        for (Map<String, Object> member :
                memberSummary) {

            double balance =
                    ((Number) member.get("balance"))
                            .doubleValue();

            if (balance < -0.01) {

                Map<String, Object> debtor =
                        new LinkedHashMap<>(member);

                debtor.put(
                        "amount",
                        round(-balance)
                );

                debtors.add(debtor);

            } else if (balance > 0.01) {

                Map<String, Object> creditor =
                        new LinkedHashMap<>(member);

                creditor.put(
                        "amount",
                        round(balance)
                );

                creditors.add(creditor);
            }
        }

        List<Map<String, Object>> settlements =
                new ArrayList<>();

        int i = 0;
        int j = 0;

        while (i < debtors.size() &&
                j < creditors.size()) {

            double debtorAmount =
                    ((Number) debtors.get(i)
                            .get("amount"))
                            .doubleValue();

            double creditorAmount =
                    ((Number) creditors.get(j)
                            .get("amount"))
                            .doubleValue();

            double amount =
                    Math.min(
                            debtorAmount,
                            creditorAmount
                    );

            Map<String, Object> settlement =
                    new LinkedHashMap<>();

            settlement.put(
                    "fromMemberId",
                    debtors.get(i).get("memberId")
            );

            settlement.put(
                    "fromName",
                    debtors.get(i).get("name")
            );

            settlement.put(
                    "fromUpiId",
                    debtors.get(i).get("upiId")
            );

            settlement.put(
                    "toMemberId",
                    creditors.get(j).get("memberId")
            );

            settlement.put(
                    "toName",
                    creditors.get(j).get("name")
            );

            settlement.put(
                    "toUpiId",
                    creditors.get(j).get("upiId")
            );

            settlement.put(
                    "amount",
                    round(amount)
            );

            settlements.add(settlement);

            debtorAmount -= amount;
            creditorAmount -= amount;

            debtors.get(i).put(
                    "amount",
                    round(debtorAmount)
            );

            creditors.get(j).put(
                    "amount",
                    round(creditorAmount)
            );

            if (debtorAmount < 0.01) {
                i++;
            }

            if (creditorAmount < 0.01) {
                j++;
            }
        }

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("total", round(total));
        result.put("memberCount", members.size());
        result.put("equalShare", round(share));
        result.put("members", memberSummary);
        result.put("settlements", settlements);

        return result;
    }


    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}