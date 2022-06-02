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

package io.service84.services.impersonation.api.rest;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.service84.library.authutils.services.AuthenticationService;
import io.service84.library.exceptionalresult.models.ExceptionalException;
import io.service84.library.standardservice.services.RequestService;
import io.service84.services.impersonation.api.ImpersonationApiDelegate;
import io.service84.services.impersonation.api.rest.exceptionalresults.InsufficientPermissionResult;
import io.service84.services.impersonation.api.rest.exceptionalresults.InternalServerError;
import io.service84.services.impersonation.dto.AssumableIdentityDTO;
import io.service84.services.impersonation.dto.AssumableIdentityPageDTO;
import io.service84.services.impersonation.dto.IdentityRequestDTO;
import io.service84.services.impersonation.exceptions.EntityNotFound;
import io.service84.services.impersonation.exceptions.InsufficientPermission;
import io.service84.services.impersonation.persistence.model.AssumableIdentity;
import io.service84.services.impersonation.services.ImpersonationService;
import io.service84.services.impersonation.services.Translator;

@Service("406F5AF5-701D-474E-BB2B-9F7363FAB7E6")
public class ImpersonationDelegate implements ImpersonationApiDelegate {
  private static Logger logger = LoggerFactory.getLogger(ImpersonationDelegate.class);

  @Autowired private ImpersonationService impersonationService;
  @Autowired private AuthenticationService authenticationService;
  @Autowired private RequestService requestService;
  @Autowired private Translator translator;

  @Override
  public ResponseEntity<AssumableIdentityDTO> assumeIdentity(
      IdentityRequestDTO body, String authentication) {
    try {
      logger.info(
          "{} {} {}",
          authenticationService.getSubject(),
          requestService.getMethod(),
          requestService.getURL());
      AssumableIdentity assumedIdentity = impersonationService.assumeIdentity(body.getIdentity());
      ResponseEntity<AssumableIdentityDTO> result =
          translator.translate(assumedIdentity, HttpStatus.OK);
      logger.info("OK");
      return result;
    } catch (InsufficientPermission e) {
      logger.info("Insufficient Permission");
      throw new InsufficientPermissionResult();
    } catch (ExceptionalException e) {
      throw e;
    } catch (Throwable t) {
      logger.error(t.getMessage(), t);
      throw new InternalServerError();
    }
  }

  @Override
  public ResponseEntity<AssumableIdentityDTO> grantAssumableIdentity(
      AssumableIdentityDTO body, String authentication) {
    try {
      logger.info(
          "{} {} {}",
          authenticationService.getSubject(),
          requestService.getMethod(),
          requestService.getURL());
      AssumableIdentity assumedIdentity =
          impersonationService.grantAssumableIdentity(body.getSubject(), body.getIdentity());
      ResponseEntity<AssumableIdentityDTO> result =
          translator.translate(assumedIdentity, HttpStatus.OK);
      logger.info("OK");
      return result;
    } catch (InsufficientPermission e) {
      logger.info("Insufficient Permission");
      throw new InsufficientPermissionResult();
    } catch (ExceptionalException e) {
      throw e;
    } catch (Throwable t) {
      logger.error(t.getMessage(), t);
      throw new InternalServerError();
    }
  }

  @Override
  public ResponseEntity<AssumableIdentityPageDTO> retrieveAssumableIdentities(
      String authentication,
      String pageIndex,
      Integer pageSize,
      List<UUID> subjects,
      List<UUID> identities) {
    try {
      logger.info(
          "{} {} {}",
          authenticationService.getSubject(),
          requestService.getMethod(),
          requestService.getURL());
      Pageable pageable = translator.getPageable(pageIndex, pageSize);
      Page<AssumableIdentity> assumedIdentityPage =
          impersonationService.retrieveAssumableIdentities(subjects, identities, pageable);
      ResponseEntity<AssumableIdentityPageDTO> result =
          translator.translateAssumableIdentityPage(assumedIdentityPage, HttpStatus.OK);
      logger.info("OK");
      return result;
    } catch (InsufficientPermission e) {
      logger.info("Insufficient Permission");
      throw new InsufficientPermissionResult();
    } catch (ExceptionalException e) {
      throw e;
    } catch (Throwable t) {
      logger.error(t.getMessage(), t);
      throw new InternalServerError();
    }
  }

  @Override
  public ResponseEntity<Void> revokeAssumableIdentity(
      AssumableIdentityDTO body, String authentication) {
    try {
      logger.info(
          "{} {} {}",
          authenticationService.getSubject(),
          requestService.getMethod(),
          requestService.getURL());
      impersonationService.revokeAssumableIdentity(body.getSubject(), body.getIdentity());
      ResponseEntity<Void> result = translator.translate(HttpStatus.NO_CONTENT);
      logger.info("No Content");
      return result;
    } catch (InsufficientPermission e) {
      logger.info("Insufficient Permission");
      throw new InsufficientPermissionResult();
    } catch (EntityNotFound e) {
      logger.info("No Content");
      return translator.translate(HttpStatus.NO_CONTENT);
    } catch (ExceptionalException e) {
      throw e;
    } catch (Throwable t) {
      logger.error(t.getMessage(), t);
      throw new InternalServerError();
    }
  }
}
