/********************************************************************************
 * Copyright (c) 2021-2022 Robert Bosch Manufacturing Solutions GmbH
 * Copyright (c) 2021-2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.semantics.hub;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

/**
 * Tests the models filter api with different filter combinations.
 * The Fuseki Server is cleared with @DirtiesContext and ensures test runs on a fresh Fuseki Server.
 */
@TestInstance( TestInstance.Lifecycle.PER_CLASS )
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_CLASS )
public class ModelsApiPaginationTest extends AbstractModelsApiTest {

   @BeforeAll
   void init(){
      deleteAllData();
   }

   @Test
   public void testGetModelsWithPaginationExpectSuccess() throws Exception {
      List<String> prefixes = List.of(
              "urn:bamm:org.eclipse.tractusx.pagination.a:1.0.0#",
              "urn:bamm:org.eclipse.tractusx.pagination.b:1.0.0#",
              "urn:bamm:org.eclipse.tractusx.pagination.c:1.0.0#",
              "urn:bamm:org.eclipse.tractusx.pagination.d:1.0.0#"
      );

      prefixes.forEach(urnPrefix -> {
         try {
            mvc.perform(post( TestUtils.createValidModelRequest(urnPrefix), "DRAFT")  )
                    .andDo( MockMvcResultHandlers.print() )
                    .andExpect( status().isOk() );
         } catch (Exception e) {
           throw new RuntimeException(e);
         }
      });


      mvc.perform(
                      MockMvcRequestBuilders.get( "/api/v1/models" )
                              .accept( MediaType.APPLICATION_JSON )
                              .with(jwtTokenFactory.allRoles())
              )
        .andDo( MockMvcResultHandlers.print() )
        .andExpect( jsonPath( "$.items" ).isArray() )
        .andExpect( jsonPath( "$.items[*]", hasSize(4) ) )
        .andExpect( jsonPath( "$.totalItems", equalTo(4) ) )
        .andExpect( jsonPath( "$.totalPages", equalTo(1) ) )
        .andExpect( jsonPath( "$.itemCount", equalTo( 4) ) )
        .andExpect( status().isOk() );

      mvc.perform(
                      MockMvcRequestBuilders.get( "/api/v1/models?pageSize=2&page=0" )
                              .accept( MediaType.APPLICATION_JSON )
                              .with(jwtTokenFactory.allRoles())
              )
              .andDo( MockMvcResultHandlers.print() )
              .andExpect( jsonPath( "$.items" ).isArray() )
              .andExpect( jsonPath( "$.items[*]", hasSize(2) ) )
              .andExpect( jsonPath( "$.items[*].urn", hasItems( toMovementUrn(prefixes.get(0)), toMovementUrn(prefixes.get(1)) ) ) )
              .andExpect( jsonPath( "$.items[*].version", hasItem( "1.0.0" ) ) )
              .andExpect( jsonPath( "$.items[*].name", hasItem( "Movement" ) ) )
              .andExpect( jsonPath( "$.items[*].type", hasItem( "SAMM" ) ) )
              .andExpect( jsonPath( "$.items[*].status", hasItem( "DRAFT" ) ) )
              .andExpect( jsonPath( "$.totalItems", equalTo(4) ) )
              .andExpect( jsonPath( "$.totalPages", equalTo(2) ) )
              .andExpect( jsonPath( "$.itemCount", equalTo( 2 ) ) )
              .andExpect( status().isOk() );

      mvc.perform(
                      MockMvcRequestBuilders.get( "/api/v1/models?pageSize=2&page=1" )
                              .accept( MediaType.APPLICATION_JSON )
                              .with(jwtTokenFactory.allRoles())
              )
              .andDo( MockMvcResultHandlers.print() )
              .andExpect( jsonPath( "$.items" ).isArray() )
              .andExpect( jsonPath( "$.items[*]", hasSize(2) ) )
              .andExpect( jsonPath( "$.items[*].urn", hasItems( toMovementUrn(prefixes.get(2)), toMovementUrn(prefixes.get(3) ) ) ))
              .andExpect( jsonPath( "$.items[*].version", hasItem( "1.0.0" ) ) )
              .andExpect( jsonPath( "$.items[*].name", hasItem( "Movement" ) ) )
              .andExpect( jsonPath( "$.items[*].type", hasItem( "SAMM" ) ) )
              .andExpect( jsonPath( "$.items[*].status", hasItem( "DRAFT" ) ) )
              .andExpect( jsonPath( "$.totalItems", equalTo(4) ) )
              .andExpect( jsonPath( "$.totalPages", equalTo(2) ) )
              .andExpect( jsonPath( "$.itemCount", equalTo( 2 ) ) )
              .andExpect( status().isOk() );

      mvc.perform(
                      MockMvcRequestBuilders.get( "/api/v1/models?pageSize=1&page=3" )
                              .accept( MediaType.APPLICATION_JSON )
                              .with(jwtTokenFactory.allRoles())
              )
              .andDo( MockMvcResultHandlers.print() )
              .andExpect( jsonPath( "$.items" ).isArray() )
              .andExpect( jsonPath( "$.items[*]", hasSize(1) ) )
              .andExpect( jsonPath( "$.items[*].urn", hasItems( toMovementUrn(prefixes.get(2) ) ) ))
              .andExpect( jsonPath( "$.items[*].version", hasItem( "1.0.0" ) ) )
              .andExpect( jsonPath( "$.items[*].name", hasItem( "Movement" ) ) )
              .andExpect( jsonPath( "$.items[*].type", hasItem( "SAMM" ) ) )
              .andExpect( jsonPath( "$.items[*].status", hasItem( "DRAFT" ) ) )
              .andExpect( jsonPath( "$.totalItems", equalTo(4) ) )
              .andExpect( jsonPath( "$.totalPages", equalTo(4) ) )
              .andExpect( jsonPath( "$.itemCount", equalTo( 1 ) ) )
              .andExpect( status().isOk() );

      mvc.perform(
                      MockMvcRequestBuilders.get( "/api/v1/models?pageSize=3" )
                              .accept( MediaType.APPLICATION_JSON )
                              .with(jwtTokenFactory.allRoles())
              )
              .andDo( MockMvcResultHandlers.print() )
              .andExpect( jsonPath( "$.items" ).isArray() )
              .andExpect( jsonPath( "$.items[*]", hasSize(3) ) )
              .andExpect( jsonPath( "$.items[*].urn", hasItems( toMovementUrn(prefixes.get(0)),
                      toMovementUrn(prefixes.get(1)),
                      toMovementUrn(prefixes.get(2)) ) ) )
              .andExpect( jsonPath( "$.items[*].version", hasItem( "1.0.0" ) ) )
              .andExpect( jsonPath( "$.items[*].name", hasItem( "Movement" ) ) )
              .andExpect( jsonPath( "$.items[*].type", hasItem( "SAMM" ) ) )
              .andExpect( jsonPath( "$.items[*].status", hasItem( "DRAFT" ) ) )
              .andExpect( jsonPath( "$.totalItems", equalTo(4) ) )
              .andExpect( jsonPath( "$.totalPages", equalTo(2) ) )
              .andExpect( jsonPath( "$.itemCount", equalTo( 3 ) ) )
              .andExpect( status().isOk() );
   }

   private static String toMovementUrn(String urn){
      return urn + "Movement";
   }

}
