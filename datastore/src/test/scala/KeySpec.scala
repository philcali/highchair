package highchair.tests

import com.google.appengine.api.datastore.KeyFactory
import org.specs._

class KeySpec extends highchair.specs.DataStoreSpec {
  
  val chris = Person(None, "Chris", Some("Aaron"), "Lewis", 29, new java.util.Date, Nil)
  
  val cleanup = () => 
    Person where (_.lastName === "Lewis") fetch() map Person.delete
  
  "an Entity" should {
    doAfter { cleanup() }
    val saved = Person.put(chris)
    
    "have no initial key" in {
      chris.key must_== None
    }
    
    "have a key when saved" in {
      Person.put(chris).key must_!= None
    }
    
    "update if it already exists" in {
      Person.put {
        Person.put {
          saved.copy(age = 30)
        } copy(age = 31)
      }
      
      (Person where (_.lastName is "Lewis") fetch() size) must_== 1
    }
  }
  
  "a query by Key" should {
    val saved = Person.put(chris)
    
    "find None when no results exist" in {
      Person.get(KeyFactory.createKey("heart", "1")) must_== None
    }
    
    "find Some when an entity matches by key" in {
      saved.key.flatMap { Person.get } must_== Some(saved)
    }
  }
  
  "a Kind" should {
    val key = Person.keyFor(1)
    
    "produce a Key for its kind" in {
      key.getKind must_== "Person"
    }
    
    "produce a Key by id" in {
      key.getId must_== 1
    }
    
    
  }
  
}
