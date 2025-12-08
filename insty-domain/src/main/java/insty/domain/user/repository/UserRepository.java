package insty.domain.user.repository;

import insty.model.user.SocialType;
import insty.model.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndSocialIdIsNull(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findBySocialIdAndSocialType(String s, SocialType socialType);

    @Query("SELECT u FROM User u "
            + "LEFT JOIN FETCH u.profileImage "
            + "WHERE u.nickname LIKE %:keyword% "
            + "AND u.isDeleted = false "
            + "AND u.id != :excludeUserId ORDER BY u.nickname"
    )
    List<User> searchUsersByKeyword(
        @Param("keyword") String keyword, 
        @Param("excludeUserId") Long excludeUserId, 
        Pageable pageable
    );
}
