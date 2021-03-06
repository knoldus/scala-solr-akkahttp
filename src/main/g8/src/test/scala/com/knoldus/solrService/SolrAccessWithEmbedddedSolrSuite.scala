package com.knoldus.solrService

import com.knoldus.solrService.factories.{BookDetails, SolrAccess, SolrClientAccess}
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.core.CoreContainer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FunSuite, Sequential}

@RunWith(classOf[JUnitRunner])
class SolrAccessWithEmbedddedSolrSuite extends FunSuite with MockitoSugar with BeforeAndAfterAll {
  Sequential

  var server: EmbeddedSolrServer = _
  var solrAccess: SolrAccess = _
  val bookDetails = BookDetails("1", Array("s.chand"), "Solr", "Henry",
    Some("education"), 2, "education", inStock = true, 1253.1D, 2569)

  override def beforeAll(): Unit = {
    val container = new CoreContainer()
    container.load()

    server = new EmbeddedSolrServer(container, "test_embedded")
    val solrClientAccess = new SolrClientAccess(server,None)

    solrAccess = new SolrAccess(solrClientAccess)

    server.deleteByQuery("*:*")
  }
  override def afterAll(): Unit = {
    server.close()
  }

  test("test insert") {
    val response = solrAccess.createOrUpdateRecord(bookDetails)
    assert(response.isDefined)
  }

  test("test find all records") {
    val response = solrAccess.findAllRecord
    assert(response.isDefined)
  }

  test("test find record with keyword") {
    val response = solrAccess.findRecordWithKeyword("Henry")
    assert(response.isDefined)
  }

  test("test find record with key and value") {
    val response = solrAccess.findRecordWithKeyAndValue("author", "Henry")
    assert(response.isDefined)

  }
}
