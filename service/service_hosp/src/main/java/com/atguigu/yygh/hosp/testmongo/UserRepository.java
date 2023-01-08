package com.atguigu.yygh.hosp.testmongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author rbx
 * @title
 * @Create 2023-01-08 10:26
 * @Description
 */
@Repository
public interface UserRepository extends MongoRepository<User,String> {

    List<User> getByNameAndAge(String name, int age);

    List<User> getByNameLike(String name);
}
