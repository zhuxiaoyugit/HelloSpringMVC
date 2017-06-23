package com.test.autoIncrement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ScriptOperations;
import org.springframework.data.mongodb.core.script.ExecutableMongoScript;
import org.springframework.data.mongodb.core.script.NamedMongoScript;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongo.entity.UserTest;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;

import static org.junit.Assert.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:beans.xml")
public class AutoIncrementIdAndScriptTest {

	@Autowired
	private MongoTemplate template;
	
	@Before
	public void setUp() throws Exception{
		ScriptOperations scriptOps=template.scriptOps();
		scriptOps.register(new NamedMongoScript("getNextSequence", "function(name){var ret=db.counters.findAndModify" +
				"({query:{_id:name},update:{$inc:{seq:NumberLong(1)}},new:true});return ret.seq.floatApprox}"));
		
		  template.upsert(query(where("_id").is("userid")), update("seq", 0L), "counters");
	}
	
	
	@Test
    public void testClientScript() {
        final String origin = "Hello World";
        String script = "function(x){return x + \".\"}";

        ExecutableMongoScript mongoScript = new ExecutableMongoScript(script);

        ScriptOperations scriptOps = template.scriptOps();

        Object result1 = scriptOps.execute(mongoScript, origin);
        // Spring使用String.format()方法对字符串进行了处理
//        System.out.println(result1);	
        assertEquals(String.format("'%s'", origin) + '.', result1);

        Object mongoEval = template.getDb().eval(script, origin);
//        System.out.println(mongoEval);
        assertEquals(origin + ".", mongoEval);

        Object result2 = scriptOps.execute(mongoScript, 3);
        assertEquals("3.", result2);
    }
    @Test
    public void testAutoIncrementIdAndStoredScript() {
        ScriptOperations scriptOps = template.scriptOps();

        boolean exists = scriptOps.exists("getNextSequence");
        assertTrue(exists);

        // JavaScript返回的总是双精度浮点型数字,所以需要转换
        UserTest jack = new UserTest(((Number) scriptOps.call("getNextSequence", "userid")).longValue(), "Jack");
        UserTest rose = new UserTest(((Number) scriptOps.call("getNextSequence", "userid")).longValue(), "Rose");
        template.insert(jack);
        template.insert(rose);

        assertEquals(1, jack.getId());
        assertEquals(2, rose.getId());

        DB db = template.getDb();
        Object eval = db.eval("getNextSequence('userid')");
        // JavaScript返回的总是双精度浮点型数字
        assertEquals(3.0d, eval);
    }


    /**
     * 注释掉此方法可以查看数据库中的集合数据。
     */
	 @After
	 public void tearDown() throws Exception {
	 template.dropCollection("counters");
	 template.dropCollection(UserTest.class);
	 template.getCollection("system.js").remove(new BasicDBObject("_id",
	 "getNextSequence"));
	 }
}
