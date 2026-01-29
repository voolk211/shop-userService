package org.example.shopuserservice.specification;

import lombok.NoArgsConstructor;
import org.example.shopuserservice.model.entities.User;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor
public class UserSpecification {

    public static Specification<User> hasName(String name){
        return (root, query, criteriaBuilder) -> {
           if (name == null || name.isBlank()){
               return criteriaBuilder.conjunction();
           }
           return criteriaBuilder.like(
                   criteriaBuilder.lower(root.get("name")),
                   "%" + name.toLowerCase().trim() + "%");
        };
    }

    public static Specification<User> hasSurname(String surname){
        return (root, query, criteriaBuilder) -> {
            if (surname == null || surname.isBlank()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("surname")),
                    "%" + surname.toLowerCase().trim() + "%");
        };
    }
}
