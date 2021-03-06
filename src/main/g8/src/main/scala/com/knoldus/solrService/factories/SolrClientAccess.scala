package com.knoldus.solrService.factories

import com.google.gson.Gson
import com.google.inject.Inject
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.{QueryResponse, UpdateResponse}
import org.apache.solr.client.solrj.{SolrClient, SolrQuery, SolrServerException}
import org.apache.solr.common.SolrInputDocument
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse

/**
  * We are using Google Juice to inject object of different class
  *
  * @param solrClientForInsert
  */
class SolrClientAccess @Inject()(solrClientForInsert: SolrClient, solrClient: Option[HttpSolrClient]) {

  val config = ConfigFactory.load("application.conf")
  val url = config.getString("solr.url")
  val collection_name = config.getString("solr.collection")

  val logger = Logger(classOf[SolrAccess])

  /**
    * This method takes a parameter of Book_Details and then insert data or update data if that is
    * present into solr collection. It match unique key and in our case that is id.
    *
    * @param book_Details
    * @return
    */
  def insertRecord(book_Details: BookDetails): Option[Int] = {
    try {
      val solrInputDocument: SolrInputDocument = new SolrInputDocument()
      solrInputDocument.addField("id", book_Details.id)
      solrInputDocument.addField("cat", book_Details.cat)
      solrInputDocument.addField("name", book_Details.name)
      solrInputDocument.addField("author", book_Details.author)
      solrInputDocument.addField("series_t", book_Details.series_t)
      solrInputDocument.addField("sequence_i", book_Details.sequence_i)
      solrInputDocument.addField("genre_s", book_Details.genre_s)
      solrInputDocument.addField("inStock", book_Details.inStock)
      solrInputDocument.addField("price", book_Details.price)
      solrInputDocument.addField("pages_i", book_Details.pages_i)
      val result: UpdateResponse = solrClientForInsert.add(collection_name, solrInputDocument)
      Some(result.getStatus)
    } catch {
      case solrServerException: SolrServerException =>
        logger.error("Solr Server Exception : " + solrServerException.getMessage)
        None
    }
  }

  /**
    * This is a method which takes the value of solrQuery and then execute query with solr
    * client and after execution it parse result into Case Class and create a List[CaseClass].
    *
    * @param keyValue : value for search
    * @return
    */
  def fetchData(keyValue: String): Option[List[BookDetails]] = {
    try {
      val parameter = new SolrQuery()
      parameter.set("qt", "/select")
      parameter.set("indent", "true")
      parameter.set("q", s"$keyValue")
      parameter.set("wt", "json")

      val gson: Gson = new Gson()

      val response: QueryResponse = if (solrClient.isDefined) {
        solrClient.get.query(parameter)
      } else {
        solrClientForInsert.query(collection_name, parameter)
      }

      implicit val formats = DefaultFormats
      val data: List[BookDetails] = parse(gson.toJson(response.getResults))
        .extract[List[BookDetails]]
      Some(data)
    } catch {
      case solrServerException: SolrServerException =>
        logger.error("Solr Server Exception : " + solrServerException.getMessage)
        None
    }
  }
}
