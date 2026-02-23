package org.example.shopuserservice.specification;

import lombok.NoArgsConstructor;
import org.example.shopuserservice.model.entities.Card;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor
public class CardSpecification {

    public static Specification<Card> cardUserHasName(String name){
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()){
                return criteriaBuilder.conjunction();
            }
            var userJoin = root.join("user");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("name")),
                    "%" + name.toLowerCase().trim() + "%");
        };
    }

    public static Specification<Card> cardUserHasSurname(String surname){
        return (root, query, criteriaBuilder) -> {
            if (surname == null || surname.isBlank()){
                return criteriaBuilder.conjunction();
            }
            var userJoin = root.join("user");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("surname")),
                    "%" + surname.toLowerCase().trim() + "%");
        };
    }

}
