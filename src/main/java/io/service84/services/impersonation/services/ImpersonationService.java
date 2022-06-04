/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.service84.services.impersonation.services;

import static io.service84.library.standardpersistence.services.SpecificationHelper.simpleTrue;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import io.service84.library.authutils.services.AuthenticationService;
import io.service84.services.impersonation.exceptions.EntityNotFound;
import io.service84.services.impersonation.exceptions.InsufficientPermission;
import io.service84.services.impersonation.persistence.model.AssumableIdentity;
import io.service84.services.impersonation.persistence.model.AssumableIdentity_;
import io.service84.services.impersonation.persistence.repository.AssumableIdentityRepository;

@Service("FCD09D82-4BDB-47D4-B783-B034E3417CE4")
public class ImpersonationService {
  private static String AssumeIdentity = "impersonation:assume_identity";
  private static String ImpersonateAnyOtherSubject = "impersonation:impersonate_any_other_subject";
  private static String RetrieveAnyAssumableIdentity =
      "impersonation:retrieve_any_assumable_identity";
  private static String GrantAnyAssumableIdentity = "impersonation:grant_any_assumable_identity";
  private static String RevokeAnyAssumableIdentity = "impersonation:revoke_any_assumable_identity";

  @Autowired private AssumableIdentityRepository repository;
  @Autowired private AuthenticationService authenticationService;

  public AssumableIdentity assumeIdentity(UUID identity) throws InsufficientPermission {
    UUID subject = UUID.fromString(authenticationService.getSubject());
    List<String> subjectScopes = authenticationService.getScopes();

    if (!subjectScopes.contains(AssumeIdentity)) {
      throw new InsufficientPermission();
    }
    if (subjectScopes.contains(ImpersonateAnyOtherSubject)) {
      return new AssumableIdentity(subject, identity);
    }
    return repository
        .findBySubjectAndIdentity(subject, identity)
        .orElseThrow(InsufficientPermission.supplier());
  }

  public AssumableIdentity grantAssumableIdentity(UUID subject, UUID identity)
      throws InsufficientPermission {
    List<String> subjectScopes = authenticationService.getScopes();

    if (subjectScopes.contains(GrantAnyAssumableIdentity)) {
      AssumableIdentity assumableIdentity = new AssumableIdentity(subject, identity);
      return repository.saveAndFlush(assumableIdentity);
    }
    throw new InsufficientPermission();
  }

  @SuppressWarnings("serial")
  private Specification<AssumableIdentity> identitySelector(List<UUID> identities) {
    if (identities == null || identities.isEmpty()) {
      return simpleTrue();
    }
    return new Specification<>() {
      @Override
      public Predicate toPredicate(
          Root<AssumableIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Predicate predicate = root.get(AssumableIdentity_.identity).in(identities);
        return predicate;
      }
    };
  }

  public Page<AssumableIdentity> retrieveAssumableIdentities(
      List<UUID> subjects, List<UUID> identities, Pageable pageable) throws InsufficientPermission {
    List<String> subjectScopes = authenticationService.getScopes();

    if (subjectScopes.contains(RetrieveAnyAssumableIdentity)) {
      return repository.findAll(
          subjectSelector(subjects).and(identitySelector(identities)), pageable);
    }
    throw new InsufficientPermission();
  }

  public void revokeAssumableIdentity(UUID subject, UUID identity)
      throws InsufficientPermission, EntityNotFound {
    List<String> subjectScopes = authenticationService.getScopes();

    if (!subjectScopes.contains(RevokeAnyAssumableIdentity)) {
      throw new InsufficientPermission();
    }
    AssumableIdentity assumableIdentity =
        repository
            .findBySubjectAndIdentity(subject, identity)
            .orElseThrow(EntityNotFound.supplier());
    repository.delete(assumableIdentity);
  }

  @SuppressWarnings("serial")
  private Specification<AssumableIdentity> subjectSelector(List<UUID> subjects) {
    if (subjects == null || subjects.isEmpty()) {
      return simpleTrue();
    }
    return new Specification<>() {
      @Override
      public Predicate toPredicate(
          Root<AssumableIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Predicate predicate = root.get(AssumableIdentity_.subject).in(subjects);
        return predicate;
      }
    };
  }
}
