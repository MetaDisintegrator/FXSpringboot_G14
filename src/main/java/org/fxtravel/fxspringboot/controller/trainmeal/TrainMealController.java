package org.fxtravel.fxspringboot.controller.trainmeal;

import jakarta.servlet.http.HttpSession;
import org.fxtravel.fxspringboot.pojo.dto.trainmeal.TrainMealQueryDTO;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.pojo.entities.trainmeal.TrainMeal;
import org.fxtravel.fxspringboot.service.inter.trainmeal.TrainMealService;
import org.fxtravel.fxspringboot.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/train/meal")
public class TrainMealController {
    @Autowired
    private TrainMealService trainMealService;

    @GetMapping("/{trainId}")
    public ResponseEntity<?> getUserMeals(@PathVariable Integer trainId,
                                                        HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "未登录"));
        }



        List<TrainMeal> results = trainMealService.getMealsByTrain4User(trainId);
        return ResponseEntity.ok(Map.of(
                "message", "查询成功",
                "data", results,
                "sortBy", "price"
        ));
    }
}
