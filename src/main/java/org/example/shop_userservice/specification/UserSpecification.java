package org.example.shop_userservice.specification;

import lombok.NoArgsConstructor;
import org.example.shop_userservice.model.User;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor
public class UserSpecification {

    public static Specification<User> hasName(String name){
        return (root, query, criteriaBuilder) -> {
           if (name == null || name.isBlank()){
               return criteriaBuilder.conjunction();
           }
           return criteriaBuilder.equal(
                   criteriaBuilder.lower(root.get("name")),
                   name.toLowerCase());
        };
    }

    public static Specification<User> hasSurname(String name){
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("surname")),
                    name.toLowerCase());
        };
    }
}
