/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thiendz.j5.assignment.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import thiendz.j5.assignment.model.Category;

/**
 *
 * @author Administrator
 */
public interface CategoryDAO extends JpaRepository<Category, Integer> {

}
