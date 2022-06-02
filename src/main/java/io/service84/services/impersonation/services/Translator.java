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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.service84.library.standardpersistence.services.PaginationTranslator;
import io.service84.services.impersonation.dto.AssumableIdentityDTO;
import io.service84.services.impersonation.dto.AssumableIdentityPageDTO;
import io.service84.services.impersonation.dto.PaginationDataDTO;
import io.service84.services.impersonation.persistence.model.AssumableIdentity;

@Service("F272207F-ABCB-49A3-ABF4-92EE391246EE")
public class Translator extends PaginationTranslator {
  public static class StandardPaginationDataDTO extends PaginationDataDTO
      implements PaginationDataStandard {}

  public AssumableIdentityDTO translate(AssumableIdentity entity) {
    if (entity == null) {
      return null;
    }

    AssumableIdentityDTO dto = new AssumableIdentityDTO();
    dto.setSubject(entity.getSubject());
    dto.setIdentity(entity.getIdentity());
    return dto;
  }

  public ResponseEntity<AssumableIdentityDTO> translate(
      AssumableIdentity entity, HttpStatus status) {
    return new ResponseEntity<>(translate(entity), status);
  }

  public ResponseEntity<Void> translate(HttpStatus status) {
    return new ResponseEntity<>(status);
  }

  public AssumableIdentityPageDTO translateAssumableIdentityPage(Page<AssumableIdentity> page) {
    if (page == null) {
      return null;
    }

    List<AssumableIdentityDTO> content =
        page.getContent().stream().map(e -> translate(e)).collect(Collectors.toList());
    return new AssumableIdentityPageDTO()
        .metadata(metadata(page, StandardPaginationDataDTO.class))
        .content(content);
  }

  public ResponseEntity<AssumableIdentityPageDTO> translateAssumableIdentityPage(
      Page<AssumableIdentity> page, HttpStatus status) {
    return new ResponseEntity<>(translateAssumableIdentityPage(page), status);
  }
}
