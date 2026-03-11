/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.hxi_connector.nucleus_sync.controller.advice;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.AlfrescoUnavailableException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.NucleusUnavailableException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.SyncException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.SyncInProgressException;

@RestControllerAdvice
public class SyncExceptionHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncExceptionHandler.class);

    // 409 - If sync already in progress
    @ExceptionHandler(SyncInProgressException.class)
    public ResponseEntity<ErrorResponse> handleSyncInProgress(SyncInProgressException e)
    {
        LOGGER.atWarn()
                .setMessage("Sync already in progress")
                .log();
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "SYNC_IN_PROGRESS",
                        e.getMessage(),
                        LocalDateTime.now()));
    }

    // 503 - If alfresco is not available
    @ExceptionHandler(AlfrescoUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAlfrescoUnavailable(AlfrescoUnavailableException e)
    {
        LOGGER.atError()
                .setMessage("Alfresco unavailable")
                .setCause(e)
                .log();
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(
                        "ALFRESCO_UNAVAILABLE",
                        e.getMessage(),
                        LocalDateTime.now()));
    }

    // 503 - If nucleus is not available
    @ExceptionHandler(NucleusUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleNucleusUnavailable(NucleusUnavailableException e)
    {
        LOGGER.atError()
                .setMessage("Nucleus unavailable")
                .setCause(e)
                .log();
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(
                        "NUCLEUS_UNAVAILABLE",
                        e.getMessage(),
                        LocalDateTime.now()));
    }

    // 500 - For sync exception
    @ExceptionHandler(SyncException.class)
    public ResponseEntity<ErrorResponse> handleSyncException(SyncException e)
    {
        LOGGER.atError()
                .setMessage("Sync failed")
                .setCause(e)
                .log();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "SYNC_FAILED",
                        e.getMessage(),
                        LocalDateTime.now()));
    }

    record ErrorResponse(String errorCode, String message, LocalDateTime timestamp)
    {}
}
